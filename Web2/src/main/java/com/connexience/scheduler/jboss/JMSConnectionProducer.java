/**
 * e-Science Central
 * Copyright (C) 2008-2013 School of Computing Science, Newcastle University
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * version 2 as published by the Free Software Foundation at:
 * http://www.gnu.org/licenses/gpl-2.0.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, 5th Floor, Boston, MA 02110-1301, USA.
 */
package com.connexience.scheduler.jboss;

import com.connexience.server.ConnexienceException;
import com.connexience.server.jms.JMSProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;


public class JMSConnectionProducer
{
    private static final Logger _Logger = LoggerFactory.getLogger(JMSConnectionProducer.class);

    private static final String RESOURCE_NAME = "java:/JmsXA";

    @Resource(mappedName = RESOURCE_NAME)
    private ConnectionFactory _connectionFactory;


    @Produces
    @JBossASConnectionFactory
    public ConnectionFactory createConnectionFactory() throws ConnexienceException
    {
        _Logger.debug("Using injected connection factory '" + RESOURCE_NAME + "'");
        return _connectionFactory;
    }

    @Produces
    Connection getJMSConnection() throws JMSException, ConnexienceException {
        // FIXME: Handle user properties like the PerformanceMonitor and Server do.
        //return JMSProperties.isUser() ?
        //        _connectionFactory.createConnection("connexience", "1234") :
        //        _connectionFactory.createConnection();
        return _connectionFactory.createConnection("connexience", "1234");
    }
}
