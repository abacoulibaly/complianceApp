"# complianceApp" created By Seydou Coulibaly 
This app helps government compliance agents verify alcohol bottle labels automatically.
 You upload a label image (JPG/PNG) plus expected details like brand name, alcohol percentage, and product type; GPT-4o vision reads the label and extracts the text.
 It then runs compliance checks—fuzzy matching for brand/product type, numeric matching for ABV, 
 and a strict check that the exact government warning phrase GOVERNMENT WARNING: appears—and returns a clear pass/fail result, including for batch uploads.

1.	Project overview and compliance rules
2.	Prerequisites (Java 21, Maven, OpenAI key)
3.	Quick start for Windows (run-local.ps1) and Linux (run-local.sh)
4.	Swagger UI usage
5.	curl examples for single and batch uploads
6.	Sample files reference
7.	Build, JAR, and Docker deployment
8.	API endpoint table
9.	Configuration reference

.....
