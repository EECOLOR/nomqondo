package org.qirx.nomqondo.common

import org.specs2.mutable.Specification
import org.qirx.nomqondo.api.Result
import org.qirx.nomqondo.api.Success

object EmailSpec extends Specification {

  "Email" should {

    "be created successful" in {
      val valid = Seq(
        "me@example.com",
        "a.nonymous@example.com",
        "name+tag@example.com",
        """"spaces may be quoted"@example.com""",
        """"Fred Bloggs"@example.com""",
        """"Joe\\Blow"@example.com""",
        """"Abc@def"@example.com""",
        """customer/department=shipping@example.com""",
        """$A12345@example.com""",
        """!def!xyz%abc@example.com""",
        """_somename@example.com""",
        """Ken.O'Brian@company.com""")

      val created = valid.map(Email.from)

      created must contain((r: Result[_, _]) => r.isSuccess).forall
    }

    "be a failure" in {
      val invalid = Seq(
        "NotAnEmail",
        "me@",
        "me.@example.com",
        ".me@example.com",
        "me@example..com",
        "me.example@com",
        """me\@example.com""",
        "@NotAnEmail",
        "\"\"test\blah\"\"@example.com",
        "\"test\rblah\"@example.com",
        "\"\"test\"\"blah\"\"@example.com",
        ".wooly@example.com",
        "wo..oly@example.com",
        "pootietang.@example.com",
        ".@example.com",
        "Ima Fool@example.com")

      val created = invalid.map(Email.from)
      created must contain((r: Result[_, _]) => r.isFailure).forall
    }

    "provide an implicit conversion to it's value" in {
      val email = "me@domain.com"
      val s = Email.from(email)
      s must beLike {
        case Success(e) => (e:String) ===email
      }
    }
  }
}