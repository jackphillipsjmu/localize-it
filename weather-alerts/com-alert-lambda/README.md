# com-alert-lambda
Handles an `S3Event` to grab an object from a source bucket and place it in a sink bucket while showing off some cool Kotlin things at the same time!

## Kotlin Highlights in this Project Include
- **Companion Object**: Think of this "object" as a static block in your code.
- **Extension functions**: Similar to C# and Gosu, Kotlin provides the ability to extend a class with new functionality without having to inherit from the class or use any type of design pattern such as Decorator. This is done via special declarations called extensions.
- **Data Class**: Alleviate the need for getters and setters and adds other cool functionality as well that you would be wary of with Java such like hash code, toString, etc.
- **Nullability Operators**: Safe call `?.` and Elvis operators `?:` are used and explained in the code comments but basically it provides easy null checking/disallows nulls to Null Pointer Exceptions are a thing of the past!
- **Java Interoperability**: Kotlin is fully backwards compatible with Java which is awesome because it won't lead to headaches trying to figure out where the language differs under the hood, for example, Scala wrote their own Collection library and it has caused issues in the past when interweaving the two languages.
 
## Build Project
 - Change into the `$BASE_DIR/localize-it/com-alert-lambda` directory.
 - Execute `./gradlew clean build` to build the code.
 - Find the built Lambda JAR in `$BASE_DIR/localize-it/com-alert-lambda/build/libs`