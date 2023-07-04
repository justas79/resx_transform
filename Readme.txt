build jar:
./gradlew clean build shadowJar

execute:

1) Unescape file. File should be in same dir. Pass file name:
java -jar build/libs/resx_file_transformer-1.0-SNAPSHOT-all.jar ProjectServiceResources.en.resx
generated: transformed_ProjectServiceResources.en.resx

2) Print Analysis :
java -jar build/libs/resx_file_transformer-1.0-SNAPSHOT-all.jar analyse ProjectServiceResources.en.resx

3) Escape file.
java -jar build/libs/resx_file_transformer-1.0-SNAPSHOT-all.jar escape transformed_ProjectServiceResources.en.resx
generated: reverted_transformed_ProjectServiceResources.en.resx