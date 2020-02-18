#include <stdio.h>
#include <stdlib.h>
#include <math.h>
#include "winbgi2.h"
#define MAXN 10

double ro,Cx,S,g,Pn,m;

void silnikwl(double t, double Y[], double F[]);
void silnikwyl(double t, double Y[], double F[]);
void vrk4( double x0, double y0[], double h, int n, void (*fun)(double, double*, double*), double y1[] );

int main()
{
    double t,v0,tn,tn1,tn2,dt,td,hd,h1,h2,Y[2],Y1[2],petla;

    printf("Podaj Vo:\n");
    scanf("%lf",&v0);
    printf("Podaj dt, td i hd:\n");
    scanf("%lf%lf%lf",&dt,&td,&hd);

    Cx=0.48;
    ro=1.225;
    m=13000;
    S=(m/(2.*6000))*(m/(2.*6000))*3.14;
    g=9.80665;
    Pn=230*m;
    petla=0.01;
    h1=h2=.0;

    //petla dla wyznaczenia optymalnych tn1 i tn2
    while(petla<1)
    {
        //wp
        Y[0]=.0;
        Y[1]=v0;
        t=.0;
        tn1=petla*td;
        tn2=(petla+0.01)*td;

        //strzaly testowe
        while(t<=tn1)
        {
            if(t>td)
            {
                h1=Y1[0];
                break;
            }
            vrk4(t,Y,dt,2,silnikwl,Y1);
            Y[0]=Y1[0];
            Y[1]=Y1[1];
            t=t+dt;
        }
        while(Y1[1]>0)
        {
            if(t>td)
            {
                h1=Y1[0];
                break;
            }
            vrk4(t,Y,dt,2,silnikwyl,Y1);
            Y[0]=Y1[0];
            Y[1]=Y1[1];
            t=t+dt;
        }
        Y[0]=.0;
        Y[1]=v0;
        Y1[0]=.0;
        Y1[1]=.0;
        t=.0;
        while(t<=tn2)
        {
            if(t>td)
            {
                h2=Y1[0];
                break;
            }
            vrk4(t,Y,dt,2,silnikwl,Y1);
            Y[0]=Y1[0];
            Y[1]=Y1[1];
            t=t+dt;
        }
        while(Y1[1]>0)
        {
            if(t>td)
            {
                h2=Y1[0];
                break;
            }
            vrk4(t,Y,dt,2,silnikwyl,Y1);
            Y[0]=Y1[0];
            Y[1]=Y1[1];
            t=t+dt;
        }
        if((h2>hd)&&(h1<hd))
        break;
        petla=petla+0.01;
    }
	printf("\ntn1=%lf h1=%lf\ntn2=%lf h2=%lf\n\n",tn1,h1,tn2,h2);



    //rownanie nieliniowe
    tn=tn2-((h2-hd)*(tn2-tn1))/(h2-h1);

    if((tn>td)||(tn<=.0))
    {
        printf("SYTUACJA NIEMOZLIWA\n");
        wait();
        return(0);
    }
    printf("Rakieta znajdzie sie na wysokosci %lf m\npo czasie %lf s gdy jej silnik\nbedzie pracowal przez %lf s\nprzy predkosci poczatkowej %lf m/s.\n",hd,td,tn,v0);
    
	 //wp dla tn
        Y[0]=.0;
        Y[1]=v0;
        t=.0;
		graphics(600,600);
		scale(.0,.0,120.,14000.);
        //strzal dla tn
        while(t<=tn)
        {
            vrk4(t,Y,dt,2,silnikwl,Y1);
			line(t,Y[0],t+dt,Y1[0]);
            Y[0]=Y1[0];
            Y[1]=Y1[1];
            t=t+dt;
        }
        while(Y1[1]>0)
        {
            vrk4(t,Y,dt,2,silnikwyl,Y1);
			line(t,Y[0],t+dt,Y1[0]);
            Y[0]=Y1[0];
            Y[1]=Y1[1];
            t=t+dt;
        }

	wait();
    return(0);
}

void silnikwyl(double t, double Y[], double F[])
{
    F[0]=Y[1];
    F[1]=-(g+(0.5*ro*S*Cx*Y[1]*Y[1])/m);
}
void silnikwl(double t,double Y[], double F[])
{
    F[0]=Y[1];
    F[1]=Pn/m-(g+(0.5*ro*S*Cx*Y[1]*Y[1])/m);
}
void vrk4( double x0, double y0[], double h, int n, void (*fun)(double, double*, double*), double y1[] )
{
	int		i;
	double	k1[MAXN], k2[MAXN], k3[MAXN], k4[MAXN];
	double	ytmp[MAXN];

	fun( x0, y0, k1);
	for ( i=0; i<n; ++i)
	{
		k1[i] *= h;
		ytmp[i] = y0[i] + k1[i]/2.0;
	}

	fun( x0+h/2.0, ytmp, k2);
	for ( i=0; i<n; ++i)
	{
		k2[i] *= h;
		ytmp[i] = y0[i] + k2[i]/2.0;
	}

	fun( x0+h/2.0, ytmp, k3);
	for ( i=0; i<n; ++i)
	{
		k3[i] *= h;
		ytmp[i] = y0[i] + k3[i];
	}

	fun( x0+h, ytmp, k4);
	for ( i=0; i<n; ++i)
		k4[i] *= h;

	for ( i=0; i<n; ++i)
		y1[i] = y0[i] + (k1[i] + 2.*k2[i] + 2.*k3[i] + k4[i])/6.;

}