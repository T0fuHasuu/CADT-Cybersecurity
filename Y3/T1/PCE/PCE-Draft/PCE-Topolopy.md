---------------------------

#### | Data Centre |

---------------------------



 					 ( DHCP )

Internet -> Boarder Gateway -> Firewall -> XGW -> MME | UMAC ( Control User Plan )

 					    |      |      |

 			 ( HSS \& HLS ) -> Router---|------|

 					    |                 172.x.x.x/16

 			 ( OCS, PCRF ) ->  RNC

 					    |

 				       Radio Tower

 				            |                 ( Air Signal -90Db )

 	                                  Device              ( 10.x.x.x/8 )



------------------

#### | Server |

------------------



 	     ( Public IP )         ( IDS + IPS )

Internet -> Boarder Gateway -------> Firewall ------> GR -> Spine

                                |        |                    |   ( Pair )

 			       NAT  Rekognition \& OCR         |----- DMZ ( Website, Mail, Chat )

 							      |

 							      |

 							      |	   ( Provisioning )

 							      |----- Server Farm ( CRM, ERP, APIM )

 							      |

 							      |----- Employee ( Software )

 





|----------------------------------------------|

**|Question 				       |**

| - should you cross BGP with another FW or not|

| - Tower-Tower make it as attenuation 	       |

|----------------------------------------------|



**------------**

###### **| Vocab |**

**------------**

1. Internet
2. Boarder Gateway
3. Firewall
4. XGW  : Extended gateway for mobile networks
5. MME  : Mobility Management Entity
6. DCW  : Router Name or Any kind
7. RNC  : Radio Network Controller
8. RT   : Radio Tower
9. GR   : Graceful Restart
10. CRM : customer relationship management
11. ERP : Enterprise Resource Planning
12. OCR : Customer Care
13. APIM: Application Programming Interface
14. Provisioning        : Ready to use
15. Entitlement Service : Transfer and Direct to Device
16. Rekognition         : Help Analyse object using AI
17. HLR : Home Location Register
18. HSS : Home Subscriber Server
19. PCRF: Policy and Charging Rules Function ( Enforce Realtime )
20. OCS : Online Charging System based on Rules



