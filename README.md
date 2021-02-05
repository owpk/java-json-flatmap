### Maven install
from project roots run:
```
 $mvn clean install
```
To add to your project
```xml
 <dependency>
     <groupId>org.owpk</groupId>
     <artifactId>json-flatmap-core</artifactId>
     <version>1.0</version>
 </dependency>
 <dependency>
     <groupId>org.owpk</groupId>
     <artifactId>json-flatmap-annotations</artifactId>
     <version>1.0</version>
 </dependency>
```
### Basic usage
 - Map json object using jackson lib  
 - Annotate fields which represents another nested object by @ObjecName (you should use @ObjectName(name = "field_name") to define name of the logical property. If value is empty String (which is the default), will try to use name of the field that is annotated)

Example json object
```json
{
   "name" : "hello",
   "nested_object" : {
      "id" : 0,
      "name" : "world" 
   }
}
```
Here its java mapping

```java
@ObjectName(name = "json")
public class Json {
   private String name;

   @ObjectName(name = "nested_object")
   private NestedObject obj;	
   //... Default constructor, getters, setters
}

@ObjectName(name = "nested_object")
public class NestedObject {
   private int id;
   private String name;
   //... Default constructor, getters, setters
}
```
Client code:
```java
import org.owpk.jsondataextruder.JsonFlatmap;
import com.google.common.collect.Multimap;
//... some class name

public void flat(String complexJsonObject) {
ObjectMapper mapper = new ObjectMapper();
        Json json = mapper.readValues("complexJsonObject", Json.class);

        Multimap<String, String> data = JsonFlatmap.flatmap(json, DefinitionConfig.DEFAULT);
        
        data.forEach((k, v) -> System.out.println(k + " : " + v));
}

// Output:
// name : hello
// id : 0
// name : world 
```
You can specify DefinitionConfig to define field names you want to add to result map,  
if there is an array of values you can define field names and its values to filter that array  

Example:

```java
DefinitionConfig cfg = 
	new DefinitionConfigBuilder(Json.class)
	.addFieldsToShow("name")
	.addNewDefinitionConfig(NestedObject.class)
        .addFieldsToShow("name")
	.build();
Multimap<String, String> result = JsonFlatmap.flatmap(, cfg);
result.forEach((k, v) -> System.out.println(k + " : " + v);

//Output:
// name : hello
// name : world
```
