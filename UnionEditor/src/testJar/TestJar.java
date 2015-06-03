package testJar;

import java.io.IOException;
import main.*;
import format.*;

public class TestJar {

	public static void main(String[] args) throws IOException {
		ConvertUnion cu = new ConvertUnion(System.in);
		FormatUnionClass fu = new FormatUnionClass(cu.getUnion(), "Union" + cu.getUnion().getName());
		
		System.out.println(fu);
	}
}
