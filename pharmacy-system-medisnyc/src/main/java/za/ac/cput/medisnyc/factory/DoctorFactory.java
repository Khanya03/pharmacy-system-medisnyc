/* Factory.java
   Utility Factory class
   Author: Lisakhanya Mpahla 230126669
   Date: 25 March 2026
*/

package za.ac.cput.medisnyc.factory;

import za.ac.cput.medisnyc.domain.Doctor;
import za.ac.cput.medisnyc.util.Helper;

public class DoctorFactory {

    public static Doctor createDoctor(String doctorId, String firstName, String lastName,
                                      String specialization, String phoneNumber, String email) {
        if (Helper.isNullOrEmpty(doctorId) || Helper.isNullOrEmpty(firstName)
                || Helper.isNullOrEmpty(lastName) || Helper.isNullOrEmpty(specialization)
                || Helper.isNullOrEmpty(phoneNumber) || Helper.isNullOrEmpty(email))
            return null;

        if (!Helper.isValidEmail(email))
            return null;

        if (!Helper.isValidPhone(phoneNumber))
            return null;

        return new Doctor.Builder()
                .setDoctorId(doctorId)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setSpecialization(specialization)
                .setPhoneNumber(phoneNumber)
                .setEmail(email)
                .build();
    }
}