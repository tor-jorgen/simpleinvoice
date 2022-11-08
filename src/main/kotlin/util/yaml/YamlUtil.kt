package util.yaml

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

object YamlUtil {
    /**
     * Create properties from a YAML file
     *
     * @param path path to the YAML file
     * @return properties
     */
    inline fun <reified T> fromYaml(path: String): T {
        return try {
            ObjectMapper()
            val mapper = ObjectMapper(YAMLFactory()).registerKotlinModule()
            mapper.readValue(File(path), T::class.java)
        } catch (e: Exception) {
            println("Illegal format for file: '$path'")
            throw e
        }
    }
}
