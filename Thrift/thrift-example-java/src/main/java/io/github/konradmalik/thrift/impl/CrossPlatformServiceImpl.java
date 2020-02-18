package io.github.konradmalik.thrift.impl;

import org.apache.thrift.TException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CrossPlatformServiceImpl implements CrossPlatformService.Iface {

    @Override
    public CrossPlatformResource get(int id)
            throws InvalidOperationException, TException {
        return resources.stream().filter(x -> x.id == id).findAny().get();
    }

    @Override
    public void save(CrossPlatformResource resource)
            throws InvalidOperationException, TException {
        resources.add(resource);
    }

    @Override
    public List<CrossPlatformResource> getList()
            throws InvalidOperationException, TException {
        return Collections.emptyList();
    }

    @Override
    public boolean ping() throws InvalidOperationException, TException {
        return true;
    }

    /**
     * For tests
     */
    private List<CrossPlatformResource> resources = new ArrayList<>();
}