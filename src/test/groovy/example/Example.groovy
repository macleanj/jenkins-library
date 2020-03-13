import org.junit.*
import com.lesfurets.jenkins.unit.*
import static groovy.test.GroovyAssert.*

class ExampleTest extends BasePipelineTest {
    def example

    @Before
    void setUp() {
        super.setUp()
        // load example
        example = loadScript("vars/Example.groovy")
    }

    @Test
    void testCall() {
        // call example and check result
        def result = example(text: "a_B-c.1")
        assertEquals "result:", "abc1", result
    }
}