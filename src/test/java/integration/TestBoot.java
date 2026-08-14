package integration;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.jupiter.api.Test;

import infrastructure.bootstrap.Start;

class TestBoot {

	@Test
	void testMain() {
		String arg[ ] = {"2305"};
		Start.main(arg);
		assertThrows(IOException.class, ()->new ServerSocket(2305));
	}

}
