/* Copyright (C) 2013 TU Dortmund
 * This file is part of LearnLib, http://www.learnlib.de/.
 * 
 * LearnLib is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 3.0 as published by the Free Software Foundation.
 * 
 * LearnLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with LearnLib; if not, see
 * <http://www.gnu.de/documents/lgpl.en.html>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.learnlib.tool.commands;

import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author falkhowar
 */
public class Heap implements Command {

    @Override
    public String cmd() {
        return "heap";
    }

    @Override
    public String help() {
        return "show heap contents.";
    }

    @Override
    public String execute(String[] parameter, Map<String, Object> heap, String retval) {
        StringBuilder sb = new StringBuilder();
        for (Entry<String,Object> e : heap.entrySet()) {
            sb.append(e.getKey()).append(": ").append(e.getValue()).append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }
    
}