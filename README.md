# RPG Mod
This is the code for the RPG Mod!

All code was created by Save by modifying and expanding the original game files. Of course, a large part of the code still belongs to the original game. You are free to use this code for learning purposes or for creating Necesse-related content

Save's Steam profile: https://steamcommunity.com/id/Aiz_Save/

All RPG_Mod content is licensed under **CC BY-NC-SA 4.0**

---

## Using RPG Mod as a Dependency

Since this mod is not publicly available on Maven Central, the best way to use it as a dependency for other mods is:

1. Download the latest `.jar` file of this mod and also of AphoreaMod
2. Add the `.jar` files to a folder inside your mod project (for example, `libs/`).

3. In your `build.gradle`, add the following to the `repositories` section:

    ```groovy
    flatDir {
        dirs 'libs'  // Replace 'libs' with your folder name
    }
    ```

4. Then, add the dependencies in the `dependencies` section:

    ```groovy
    compileOnly files('libs/rpgmod.jar')        // Replace with the actual jar filename
    compileOnly files('libs/aphoreamod.jar')    // Replace with the actual jar filename
    ```

5. Add both `.jar` files to your IDE's libraries so you can easily use them while coding
6. In your `build.gradle`, make sure to include `"aizsave.rpgmod"` in `project.ext.modDependencies = []`
7. Once you launch the mod, don’t forget to add the dependency to Steam
