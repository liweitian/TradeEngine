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

import java.text.DecimalFormat;
import java.util.Random;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;

public class FileCopierWithCamel {

	  public static void main(String args[]) throws Exception {
		  
		  	final TicData ticIBM = new TicData("IBM");
		  	final TicData ticMSFT = new TicData("MSFT");
		  	final TicData ticORCL = new TicData("ORCL");
		  	final DecimalFormat formatter = new DecimalFormat("0.00");
		  	
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
	                from("jms:queue:Final_Queue")
	                .process(new Processor(){
						@Override
						public void process(Exchange e) throws Exception {
							String[] data = e.getIn().getBody(String.class).split("signed");
							if(data.length==2){
	            		      	int password = Integer.parseInt(data[1]);
	            		      	
	            		        String body = e.getIn().getBody(String.class);
	            		        StringBuilder sb = new StringBuilder("");
	            		        for(int i = 0; i < body.length();i++){
	            		        	char tmp =(char) (body.charAt(i)-password);
	            		        	sb.append(tmp);
	            		        }
	            		        e.getIn().setBody(sb);
							}
							else{
								e.getIn().setBody("invalid");
							}
						}
	                })
	                .log("RECEIVED:  jms queue: ${body} from file: ${header.CamelFileNameOnly}")        
	                .choice()
	                .when(body().regex(".*IBM.*"))
	                .process(new Processor(){
						@Override
						public void process(Exchange e) throws Exception {
							ticIBM.ticMsgProcess(e.getIn().getBody(String.class));
							StringBuilder sb = new StringBuilder();
							sb.append(ticIBM.getName() + "\t");
							ticIBM.setStrategy(new CalculateMax());
							sb.append(formatter.format(ticIBM.getBidPrice()) + "\t"); 
							sb.append(formatter.format(ticIBM.getAskPrice()) + "\t");
							
							ticIBM.setStrategy(new CalculateMin());
							sb.append(formatter.format(ticIBM.getBidPrice()) + "\t"); 
							sb.append(formatter.format(ticIBM.getAskPrice()) + "\t");
							
							ticIBM.setStrategy(new CalculateMean());
							sb.append(formatter.format(ticIBM.getBidPrice()) + "\t"); 
							sb.append(formatter.format(ticIBM.getAskPrice()) + "\t");
							e.getIn().setBody(sb);
							System.out.println(ticIBM.getName() + sb.toString());
						}
	                }).to("jms:topic:Final_Topic_IBM")
	                
	                .when(body().regex(".*MSFT.*")).process(new Processor(){
						@Override
						public void process(Exchange e) throws Exception {
							// TODO Auto-generated method stub
							ticMSFT.ticMsgProcess(e.getIn().getBody(String.class));
							StringBuilder sb = new StringBuilder();
							sb.append(ticMSFT.getName() + "\t");
							ticMSFT.setStrategy(new CalculateMax());
							sb.append(formatter.format(ticMSFT.getBidPrice()) + "\t"); 
							sb.append(formatter.format(ticMSFT.getAskPrice()) + "\t");
							
							ticMSFT.setStrategy(new CalculateMin());
							sb.append(formatter.format(ticMSFT.getBidPrice()) + "\t"); 
							sb.append(formatter.format(ticMSFT.getAskPrice()) + "\t");
							
							ticMSFT.setStrategy(new CalculateMean());
							sb.append(formatter.format(ticMSFT.getBidPrice()) + "\t"); 
							sb.append(formatter.format(ticMSFT.getAskPrice()) + "\t");
							e.getIn().setBody(sb);
							System.out.println(ticMSFT.getName() + sb.toString());
						}
	                }).to("jms:topic:Final_Topic_MSFT")
	            	
	                .when(body().regex(".*ORCL.*")).process(new Processor(){
						@Override
						public void process(Exchange e) throws Exception {
							
							ticORCL.ticMsgProcess(e.getIn().getBody(String.class));
							StringBuilder sb = new StringBuilder();
							sb.append(ticORCL.getName() + "\t");
							ticORCL.setStrategy(new CalculateMax());
							sb.append(formatter.format(ticORCL.getBidPrice()) + "\t"); 
							sb.append(formatter.format(ticORCL.getAskPrice()) + "\t");
							
							ticORCL.setStrategy(new CalculateMin());
							sb.append(formatter.format(ticORCL.getBidPrice()) + "\t"); 
							sb.append(formatter.format(ticORCL.getAskPrice()) + "\t");
							
							ticORCL.setStrategy(new CalculateMean());
							sb.append(formatter.format(ticORCL.getBidPrice()) + "\t"); 
							sb.append(formatter.format(ticORCL.getAskPrice()) + "\t");
							e.getIn().setBody(sb);
							System.out.println(ticORCL.getName() + sb.toString());
						}
	                }).to("jms:topic:Final_Topic_ORCL")
	                .otherwise()
                	.to("jms:queue: Invalid_Data");
	                
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
