package test;

import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class TestCase {

	@Test
	public void test(){
		System.out.println(Duration.of(1, ChronoUnit.HOURS).toMillis());

	}
}
