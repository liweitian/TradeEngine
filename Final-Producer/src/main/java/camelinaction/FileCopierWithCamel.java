/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package camelinaction;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;

import java.util.Random;


public class FileCopierWithCamel {

	  public static void main(String args[]) throws Exception {
	        // create CamelContext
	        CamelContext context = new DefaultCamelContext();

	        // connect to ActiveMQ JMS broker listening on localhost on port 61616
	        ConnectionFactory connectionFactory = 
	        	new ActiveMQConnectionFactory("tcp://localhost:61616");
	        context.addComponent("jms",
	            JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
	        
	        // add our route to the CamelContext
	       
	        context.addRoutes(new RouteBuilder() {
	            public void configure() {
	            	from("file:data/inbox?noop=true")
	            	.filter(header("CamelFileName").regex(".*IBM.*|.*MSFT.*|.*ORCL.*"))
	            	.filter(header("CamelFileName").endsWith(".csv"))
	            	.log("RETRIEVED: ${file:name}")
	            	.unmarshal().csv()
	            	.split(body())
	            	.process(new Processor(){
	            		public void process(Exchange e) {
	            			System.out.println("File name: " 
	            					+ e.getIn().getHeader("CamelFileName") 
	            					+ " is heading to Final Queue for Stock: "
	            					+ (e.getIn().getBody(String.class).split("\t"))[0].substring(1));
	            			    
	            		        Random random = new Random();
	            		        int max=3;
	            		        int min=1;
	            		        int s = random.nextInt(max)%(max-min+1) + min;
	            		         s = 1;
	            		        System.out.println(s);
	            		        String body = e.getIn().getBody(String.class);
	            		        StringBuilder sb = new StringBuilder("");
	            		        for(int i = 0; i < body.length();i++){
	            		        	char tmp =(char) (body.charAt(i)+s);
	            		        	sb.append(tmp);
	            		        }
	            		        
	            			String signedMessage = sb + "signed"+s;
	            			
	            			e.getIn().setBody(signedMessage);
	            		}
	            	})
	            	.to("jms:queue:Final_Queue");
	            	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	            }
	        });

	        // start the route and let it do its work
	        context.start();
	        Thread.sleep(50000);

	        // stop the CamelContext
	        context.stop();
	    }
}

