package testJar;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import main.*;
import format.*;

public class TestJar {

	public static void main(String[] args) throws IOException {
		ConvertUnion cu = new ConvertUnion(System.in);
		//FormatUnionClass fu = new FormatUnionClass(cu.getUnion(), "Union" + cu.getUnion().getName());
		FormatUnionVariants fv = new FormatUnionVariants(cu.getUnion());
		
		System.out.println(fv);
	}
}
