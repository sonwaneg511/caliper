package com.caliper.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.caliper.bigquery.service.ProjectDataHelper;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Component
public class GoogleBucket {

	@Autowired
	private ProjectDataHelper projectDataHelper;

	// public String uploadTOBucket(String bucketName, String imageUrl) {
	//
	//  //Forming Object name that is file name using the url
	//  String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
	//  String objectName =fileName.contains(".") ? fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
	//
	//  Storage st = null;
	//  String publicUrl = "";
	//  // Load GCP credentials
	//  try (InputStream bQCredentialsFile = projectDataHelper.getJsonFile("gmb_ia")) {
	//   st = StorageOptions.newBuilder()
	//     .setCredentials(ServiceAccountCredentials.fromStream(bQCredentialsFile))
	//     .build()
	//     .getService();
	//  } catch (Exception e) {
	//   System.err.println("Failed to load credentials:");
	//   e.printStackTrace();
	//  }
	//
	//  // Download image from URL and upload to GCS
	//  try {
	//   URL url = new URL(imageUrl);
	//   HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	//   connection.setRequestMethod("GET");
	//   connection.setRequestProperty("User-Agent", "Mozilla/5.0");
	//
	//   try (InputStream inputStream = connection.getInputStream()) {
	//    String contentType = connection.getContentType();
	//
	//    BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, objectName)
	//      .setContentType(contentType)
	//      .build();
	//
	//    Blob create = st.create(blobInfo, inputStream);
	//    System.out.println("Image uploaded from URL to GCS: " + objectName);
	//    
	//    publicUrl = "https://storage.googleapis.com/" + bucketName + "/" + objectName;
	//    System.out.println("Public URL: " + publicUrl);
	//
	//    //System.out.println(create.getMediaLink());
	//
	//   }
	//  } catch (Exception e) {
	//   publicUrl = null;
	//   System.err.println("Failed to download or upload image:");
	//   e.printStackTrace();
	//  }
	//
	//  return publicUrl;
	// }

	public File convertMultiPartFile(MultipartFile file) throws IOException {
		File convFile = File.createTempFile("upload_", "_" + file.getOriginalFilename());
		file.transferTo(convFile);
		return convFile;
	}

	public String uploadToBucket(String bucketName, File file) throws IOException {
		String fileName = file.getName(); // Keep full name including extension
		String contentType = Files.probeContentType(file.toPath());
		String publicUrl = "";

		if (contentType == null) {
			contentType = "application/octet-stream"; // default if unknown
		}
		String serviceAccountJson = """
				{
 	"type": "service_account",
 	"project_id": "gmb-ia-123456",
 	"private_key_id": "b662c22e9ce9253f3f62e2819616895c190b2fdf",
 	"private_key": "-----BEGIN PRIVATE KEY-----
 MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDVddAfSg4Cz4U+
 V+B0U4IEXx+eb6NPY2nEK8rbaLxuX7Ugxpw6RjKKW5WyadsgGOMolQQ6GGukoiXj
 YqPdyR9uS6X9E+b/4mrMOGMpD50jRudcjR47YnRwv1XY9YLz8xlL5h7xbhZ2F1aJ
 6Z86MBQaUHubOsfS511FVgJIWjsGkAfqFLGrAalHYhLiiHkFi6uS32nzvP8rJ1gl
 NRajDdsCJaw8+b9Xrr8RgJSp0zwc1/6GpAb9n+sYSzfCC7H0o1CcR4BjwTxjJzXR
 VCsefbD5AW8fpuwzSDJUn8+M6Fo703cF/YIbiOMMwcyuKRZTIJ5dMuuOm5wuIlH1
 l3WkgDK9AgMBAAECggEALd43fwm4MgVqocdbTVPGa+8Oyco+Yug/SW6JlBUQ7Hy3
 XJgg6Oq9oHpmk+RoOCDw6v7+XgdyVfmbv50qDhXSIa6yt8uHhzOddE2njSK0RujM
 /Ve7h9IzqCiNJ2Y2Ifp6VJNZ0dIwlGaWa2Memm/LWg4ySVxbpOefZK9DJU3yQgnb
 SPb+Fink2o5rBSGFmn6lZPnfKx5bY64QUM3P9OxCnlTwKUXeojtUE6QbcAkrStHu
 fjUN4A4iE0O7WLnZR4/k8hVVFRHCBZyHwBJG1eifhzxgKBkqchsGP2xOXaMWUKXl
 rPaGotL1HC/AksxCGatk70dgb5dK54ykZg3d13iJYQKBgQD9LbE3eMKI5vh3t+Yu
 jik8pLryizGpmmLRQaE50G/oHy7G9qD6JwvS1vsiYhbFuOoEq1YZipo46p+XD4m1
 VxOkGvgqA+hjwbX7EeDUj/CLsA95LXRfJPWJwWYI0KBSbFfDbJ1WIHFDMCEFZFM8
 /Q31fO04ZJNtRDPHzDInC+duLQKBgQDX1s5d02QcSY6dyIqgj1I4VCDrQr3OlGd9
 LMfLSQKcqfI99IiQQIjGvrHIVcb2tpVlJzxK9T6TDvq4gxOGQ/AHge6Y1k05UNsM
 sMepRBxbk+7/4+c6Km1yg08xzJ/xa1VgDYh/y8M7Ru4Ktj8WMLvyJpvRKNGlxwKH
 KvH3O5RA0QKBgFrUgbe9A03KT2nj+BjLt8+h6dAKUA7g7ILWYNk1BnDofweyg547
 U+qYZhC30COva4WxUnpnV10ED61KTcVZMBq38kBglobzgC5LA7Y+zbXPvD1lKVLZ
 F54MXGbdMidf5zydQvUbPoc0RSSCEbrXrqJb519L9fhFd64+e9+2TZkhAoGBAJV4
 e3c8ckKoqewYue/Q8OZzVEy37owF99kWztG3oOhvRIOlOYF0P4QrASE0TdICxVjE
 oBtk1M2wfO6UT0wW9j2svFf5aIc9uESEk/Bja6P7wXJSGIoOYbcAujJJe6YLZ5da
 J+IgcPY2+5hNd0jdvPpHxHZHZ0//jn+evOwyxUtRAoGBAL2wLzBP9FEB8zPFXynk
 sQir5fb4Vol/6fwI3Q3dVCqRxQiv8N+XyU5PVAX6Uhd5+6Eu9gAPmFMz3sG/sMTy
 mfQzEkeMdNUrbIjxzy1NJNeQ1hHLuR1ofkMZTFHhbKWTGI/9tI7rS/WSdrkg5AAb
 iuqCzA/RUmFzDvmwPkKoOsQ3
 -----END PRIVATE KEY-----
 ",
 	"client_email": "sem-orm@gmb-ia-123456.iam.gserviceaccount.com",
 	"client_id": "104072286555531311399",
 	"auth_uri": "https://accounts.google.com/o/oauth2/auth",
 	"token_uri": "https://oauth2.googleapis.com/token",
 	"auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
 	"client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/sem-orm%40gmb-ia-123456.iam.gserviceaccount.com"
 }
				""";

				Storage storage;

				try (InputStream credentialsFile =
				         new ByteArrayInputStream(serviceAccountJson.getBytes(StandardCharsets.UTF_8))) {

				    storage = StorageOptions.newBuilder()
				            .setCredentials(ServiceAccountCredentials.fromStream(credentialsFile))
				            .build()
				            .getService();
				}
//		Storage storage;
//		try (InputStream credentialsFile = projectDataHelper.getJsonFile(ProjectDataHelper.GMB_IA)) {
//			storage = StorageOptions.newBuilder()
//					.setCredentials(ServiceAccountCredentials.fromStream(credentialsFile))
//					.build()
//					.getService();
//		}

		// Upload to GCS
		try (InputStream inputStream = new FileInputStream(file)) {
			BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
					.setContentType(contentType)
					.build();

			storage.create(blobInfo, inputStream);
			System.out.println("File uploaded to GCS: https://storage.googleapis.com/" + bucketName + "/" + fileName);

			publicUrl = "https://storage.googleapis.com/" + bucketName + "/" + fileName;

		}catch (Exception e) {
			publicUrl = null;
			System.err.println("Failed to download or upload image:");
			e.printStackTrace();
		}

		return publicUrl;
	}  

	public String saveTempFile(MultipartFile multipartFile) throws IllegalStateException, IOException{
		String tempDir = System.getProperty("java.io.tmpdir");
		String fileName = System.currentTimeMillis() + "_" + multipartFile.getOriginalFilename();
		File tempFile = new File(tempDir + File.separator + fileName);

		multipartFile.transferTo(tempFile);

		return tempFile.toURI().toURL().toString();
	}

	public void deleteTempFile(String fileUrl) {
		try {
			File file = new File(new URL(fileUrl).getPath());
			if (file.exists()) {
				file.delete();
			}
		} catch (Exception ignored) {}
	}
}
