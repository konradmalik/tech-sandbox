package main

import (
	"fmt"
	"os"
	"os/exec"
	"syscall"
)

// where do we have a basic chroot location? (aka base image?)
const baseOs = "ubuntu-base-18.04.3-base-amd64.tar.gz"
const chrootLoc = "containerRoot"
const containerHost = "container"

// docker run <container> cmd args
// go run main.go run cmd args
func main() {
	switch os.Args[1] {
	case "run":
		run()
	case "fork":
		fork()
	default:
		panic("wtf?")
	}
}

func run() {
	cmd := exec.Command("/proc/self/exe", append([]string{"fork"}, os.Args[2:]...)...)
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr
	cmd.SysProcAttr = &syscall.SysProcAttr{
		Cloneflags: syscall.CLONE_NEWUTS | syscall.CLONE_NEWPID | syscall.CLONE_NEWNS,
	}

	must(unTarGz(baseOs, chrootLoc))
	must(cmd.Run())
	// clean rootfs
	must(os.RemoveAll(chrootLoc))
}

func fork() {
	fmt.Printf("running %v as PID %d\n", os.Args[2:], os.Getpid())

	cmd := exec.Command(os.Args[2], os.Args[3:]...)
	cmd.Stdin = os.Stdin
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	must(syscall.Sethostname([]byte(containerHost)))
	must(syscall.Chroot(chrootLoc))
	must(os.Chdir("/"))

	// not sure why but must set this manually
	must(syscall.Setenv("PATH", "/bin:/sbin:/usr/local/sbin:/usr/local/bin:/usr/bin"))
	must(syscall.Mount("proc", "proc", "proc", 0, ""))
	must(cmd.Run())
	must(syscall.Unmount("proc", 0))
}

func must(err error) {
	if err != nil {
		panic(err)
	}
}

func unTarGz(tarName, targetFolder string) error {
	err := os.MkdirAll(targetFolder, os.ModeDir)
	if err != nil {
		return err
	}
	cmd := exec.Command("tar", "xvzf", tarName, "-C", targetFolder)
	err = cmd.Run()
	return err
}
