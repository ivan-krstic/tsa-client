package me.krstic.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.apache.pdfbox.io.IOUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import me.krstic.model.Digital;
import me.krstic.repository.DigitalRepository;

@Service
public class TimestampService {
	
	private static Logger LOG = LoggerFactory.getLogger(TimestampService.class);
	
	@Value("${tsa.url}")
	private String tsaUrl;
	@Value("${tsa.username}")
	private String username;
	@Value("${tsa.password}")
	private String password;
	@Value("${http.proxy.host}")
	private String httpProxyHost;
	@Value("${http.proxy.port}")
	private String httpProxyPort;
	
	private DigitalRepository digitalRepository;
	
	@Autowired
	public TimestampService(DigitalRepository digitalRepository) {
		this.digitalRepository = digitalRepository;
	}

	public ServiceResponse getTimeStampToken(Long id) {
		System.setProperty("http.proxyHost", httpProxyHost);
        System.setProperty("http.proxyPort", httpProxyPort);
		
		Digital digital = digitalRepository.findOne(id);
		
		if (digital == null) {
			LOG.error("Document with ID: " + id + " doesn't exists.");
			return new ServiceResponse(404, "Document with ID: " + id + " doesn't exists.");
		}

		MessageDigest digest = null;
		try {
			digest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e1) {
			LOG.error("NoSuchAlgorithmException: " + e1);
			return new ServiceResponse(400, "NoSuchAlgorithmException.");
		}
		
		digest.reset();
		byte[] hash = digest.digest(Base64.getDecoder().decode(digital.getData()));
		
		// 32-bit cryptographic nonce
		SecureRandom random = new SecureRandom();
		int nonce = random.nextInt();
		
		// generate TSA request
		TimeStampRequestGenerator tsaRequestGenerator = new TimeStampRequestGenerator();
		tsaRequestGenerator.setCertReq(true);
		ASN1ObjectIdentifier oid = getHashObjectIdentifier(digest.getAlgorithm());
		TimeStampRequest request = tsaRequestGenerator.generate(oid, hash, BigInteger.valueOf(nonce));
		
		try {
			Files.write(Paths.get("C:\\my-works\\Digital\\TimestampServer\\Test\\4\\testJavaRequest.tsq"), request.getEncoded());
		} catch (IOException e2) {
			LOG.error("IOException: " + e2);
			return new ServiceResponse(400, "IOException.");
		}
		
		// get TSA response
		byte[] tsaResponse = null;
		try {
			tsaResponse = getTSAResponse(request.getEncoded());
		} catch (IOException e1) {
			LOG.error("Response does not have a response.: " + e1);
			return new ServiceResponse(503, "Posta TSA Unavailable.");
		}
		
		TimeStampResponse response = null;
		try {
			response = new TimeStampResponse(tsaResponse);
			response.validate(request);
		} catch (TSPException | IOException e) {
			LOG.error("TSPException: " + e);
			return new ServiceResponse(400, "TSP Exception.");
		}

		TimeStampToken token = response.getTimeStampToken();
		if (token == null) {
			LOG.error("Response does not have a timestamp token.");
			return new ServiceResponse(400, "Response does not have a timestamp token.");
		}
		
		TimeStampTokenInfo info = token.getTimeStampInfo();
		
		LOG.info("Digest Algorithm OID: " + info.getMessageImprintAlgOID());
     	LOG.info("Time: " + info.getGenTime());
     	LOG.info("TSA: " + info.getTsa());
     	LOG.info("TimeStampInfo: " + token.getTimeStampInfo().getTsa());
     	
/*   	CREATE File from Response byte[]  	
     	Path path = Paths.get("C:\\my-works\\Digital\\TimestampServer\\Test\\4\\testJavaResponse.tsr");
     	
		try {
			Files.write(path, tsaResponse);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/		
		digital.setTsaResponse(Base64Converter.encodeBase64(tsaResponse));

/*		
		Path path = Paths.get("C:\\my-works\\Digital\\TimestampServer\\Test\\4\\testJavaResponse.tsr");
		try {
			Files.write(path, Base64Converter.decodeBase64(digital.getTsaResponse().getBytes()));
		} catch (IOException e) {
			LOG.error("Error creating TSAResponse.tsr");
			return new ServiceResponse(400, "Error creating TSAResponse.tsr");
		}
*/		
		digitalRepository.save(digital);
		
		return new ServiceResponse(200, "OK");
	}
	
	// gets response data for the given encoded TimeStampRequest data
	// throws IOException if a connection to the TSA cannot be established
	private byte[] getTSAResponse(byte[] request) throws IOException {
		LOG.debug("Opening connection to TSA server");
		
		// todo: support proxy servers
		URLConnection connection = new URL(tsaUrl).openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestProperty("Content-Type", "application/timestamp-query");
		LOG.debug("Established connection to TSA server");

		if (username != null && password != null) {
			if (!username.isEmpty() && !password.isEmpty()) {
				String basicAuth = "Basic " + new String(Base64.getEncoder().encodeToString(username.concat(":").concat(password).getBytes()));
				connection.setRequestProperty("Authorization", basicAuth);
			}
		}

		// read response
		OutputStream output = null;
		try {
			output = connection.getOutputStream();
			output.write(request);
		} finally {
			IOUtils.closeQuietly(output);
		}

		LOG.debug("Waiting for response from TSA server");
		InputStream input = null;
		byte[] response;
		try {
			input = connection.getInputStream();
			response = IOUtils.toByteArray(input);
		} finally {
			IOUtils.closeQuietly(input);
		}
		LOG.debug("Received response from TSA server");
		return response;
	}

	// returns the ASN.1 OID of the given hash algorithm
	private ASN1ObjectIdentifier getHashObjectIdentifier(String algorithm) {
		// TODO can bouncy castle or Java provide this information?
		if (algorithm.equals("MD2")) {
			return new ASN1ObjectIdentifier("1.2.840.113549.2.2");
		} else if (algorithm.equals("MD5")) {
			return new ASN1ObjectIdentifier("1.2.840.113549.2.5");
		} else if (algorithm.equals("SHA-1")) {
			return new ASN1ObjectIdentifier("1.3.14.3.2.26");
//			return new ASN1ObjectIdentifier("1.3.6.1.4.1.99999.11.800.1.0");
		} else if (algorithm.equals("SHA-224")) {
			return new ASN1ObjectIdentifier("2.16.840.1.101.3.4.2.4");
		} else if (algorithm.equals("SHA-256")) {
			return new ASN1ObjectIdentifier("2.16.840.1.101.3.4.2.1");
		} else if (algorithm.equals("SHA-394")) {
			return new ASN1ObjectIdentifier("2.16.840.1.101.3.4.2.2");
		} else if (algorithm.equals("SHA-512")) {
			return new ASN1ObjectIdentifier("2.16.840.1.101.3.4.2.3");
		} else {
			return new ASN1ObjectIdentifier(algorithm);
		}
	}
}
