/*
 *    uDig - User Friendly Desktop Internet GIS client
 *    http://udig.refractions.net
 *    (C) 2012, Refractions Research Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the Refractions BSD
 * License v1.0 (http://udig.refractions.net/files/bsd3-v10.html).
 */
package org.locationtech.udig.tutorials.catalog.csv;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.locationtech.udig.catalog.IGeoResource;
import org.locationtech.udig.catalog.IGeoResourceInfo;
import org.locationtech.udig.catalog.IService;

import org.eclipse.core.runtime.IProgressMonitor;

public class CSVGeoResource extends IGeoResource {

    public URL url;
    private CSV csv;

    public CSVGeoResource( CSVService service ) {
        this.service = service;
        File file = service.getFile();
        try {
            url = new URL( service.getIdentifier()+"#"+file.getName() );
        } catch (MalformedURLException e) {            
        }
    }

    @Override
    public URL getIdentifier() {
        return url;
    }

    @Override
    public CSVGeoResourceInfo getInfo( IProgressMonitor monitor ) throws IOException {
        return (CSVGeoResourceInfo) super.getInfo(monitor);
    }
    
	protected CSVGeoResourceInfo createInfo( IProgressMonitor monitor ) throws IOException {
    	return new CSVGeoResourceInfo( this, monitor );
    }
    
    public Throwable getMessage() {
        return service.getMessage();
    }

    public Status getStatus() {
        return service.getStatus();
    }

    /**
     * CSV API used to access the file.
     * @param monitor
     * @return CSV
     * @throws IOException
     */
    public CSV getCSV( IProgressMonitor monitor ) throws IOException {
        if (csv == null) { // lazy creation
            synchronized (this) { //support concurrent access
                if (csv == null) {
                    csv = new CSV( service(monitor).getFile() );
                }
            }
        }
        return csv;
    }
    
    @Override
    public CSVService service(IProgressMonitor monitor) throws IOException {
    	return (CSVService)super.service(monitor);
    }
    
    @Override
    public <T> boolean canResolve( Class<T> adaptee ) {
        return adaptee.isAssignableFrom(CSV.class) || super.canResolve(adaptee);
    }
    @Override
    public <T> T resolve( Class<T> adaptee, IProgressMonitor monitor ) throws IOException {
        if( adaptee.isAssignableFrom(CSV.class) ){
            return adaptee.cast( getCSV( monitor) );
        }
        return super.resolve(adaptee, monitor);
    }
}
