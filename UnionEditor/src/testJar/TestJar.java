package testJar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import main.*;
import format.*;

public class TestJar {

	public static void main(String[] args) throws IOException {
		ConvertUnion cu = new ConvertUnion(System.in);
		FormatUnionClass f = new FormatUnionClass(cu.getUnion(), "Union" + cu.getUnion().getName());
		//FormatUnionVariants f = new FormatUnionVariants(cu.getUnion());
		
		System.out.println(f);
	}
}
