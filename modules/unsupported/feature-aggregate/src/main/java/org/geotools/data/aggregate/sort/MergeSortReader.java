/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.aggregate.sort;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.simple.SimpleFeatureReader;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class MergeSortReader implements SimpleFeatureReader {

    List<FeatureFileReader> readers;

    SimpleFeatureType schema;

    Comparator<SimpleFeature> comparator;

    public MergeSortReader(SimpleFeatureType schema, List<File> files,
            Comparator<SimpleFeature> comparator) throws IOException {
        this.schema = schema;
        this.comparator = comparator;
        this.readers = new ArrayList<FeatureFileReader>();
        for (File file : files) {
            readers.add(new FeatureFileReader(file, schema));
        }
    }

    @Override
    public SimpleFeatureType getFeatureType() {
        return schema;
    }

    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        if (readers.size() == 0) {
            throw new NoSuchElementException();
        }

        // find the smallest feature
        int selected = 0;
        for (int i = 1; i < readers.size(); i++) {
            SimpleFeature sf = readers.get(selected).feature();
            SimpleFeature cf = readers.get(i).feature();
            if (comparator.compare(sf, cf) > 0) {
                selected = i;
            }
        }

        // move on the reader of the selected feature
        FeatureFileReader reader = readers.get(selected);
        SimpleFeature sf = reader.feature();
        if (reader.next() == null) {
            readers.remove(selected);
        }

        // return the selected feature
        return sf;
    }

    @Override
    public boolean hasNext() throws IOException {
        return readers.size() > 0;
    }

    @Override
    public void close() throws IOException {
        for (FeatureFileReader reader : readers) {
            reader.close();
        }
    }

}