package com.api.gateway;
//package com.marshpride.partnergateway;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.dataformat.xml.XmlMapper;
//import com.marshpride.partnergateway.dto.PartnerConfig;
//import com.marshpride.partnergateway.util.Constants;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.util.ArrayList;
//import java.util.Base64;
//import java.util.List;
//import java.util.Map;
//
//public class Test {
//  public static void main(String[] args) {
//    XmlMapper xmlMapper = new XmlMapper();
//    String xmlString = "<Request>\n" + " <Id>1</Id>\n" + " <Name>arnay</Name>\n" + " <Data>\n"
//        + " <DoB>30-10-1995</DoB>\n" + " <Age>27</Age>\n" + " <Address>\n"
//        + " <Pincode>122003</Pincode>\n" + " <City>Gurugram</City>\n" + " </Address>\n"
//        + " </Data>\n" + " <PlanId>\n" + " <Item>1</Item>\n" + " <Item>2</Item>\n"
//        + " <Item>3</Item>\n" + " </PlanId>\n" + " <PaymentMethod>UPI</PaymentMethod>\n"
//        + " <PaymentMethod>CC</PaymentMethod>\n" + " <PaymentMethod>DC</PaymentMethod>\n"
//        + "</Request>";
//    try {
//      Map<String, Object> map = xmlMapper.readValue(xmlString, Map.class);
//      System.out.println(map);
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//}
