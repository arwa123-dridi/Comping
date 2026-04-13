package tn.comping.spring.backendcomping.dto;

import lombok.*;
import tn.comping.spring.backendcomping.entities.Role;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignupDTO {

    private String name;
    private String email;
    private String password;
    private String telephone;
    private String address;
    private Role role;

    //public SignupDTO() {}

  //  public String getName() {
   //     return name;
  //  }

    //public String getEmail() {
     //   return email;
   // }

    //public String getPassword() {
    //    return password;
    //}

    //public String getTelephone() {
   //     return telephone;
   // }

  //  public String getAddress() {
     //   return address;
   // }

   // public String getRole() {
     //   return role;
   // }

   // public void setName(String name) {
     //   this.name = name;
   // }

   // public void setEmail(String email) {
    //    this.email = email;
   // }

   // public void setPassword(String password) {
   //     this.password = password;
   // }

   // public void setTelephone(String telephone) {
    //    this.telephone = telephone;
    //}

    //public void setAddress(String address) {
     //   this.address = address;
   // }

   // public void setRole(String role) {
    //    this.role = role;
    //}
}