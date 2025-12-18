package edu.mondragon.we2.pinkAlert.config;

import edu.mondragon.we2.pinkAlert.model.*;
import edu.mondragon.we2.pinkAlert.repository.*;
import edu.mondragon.we2.pinkAlert.service.UserService;

import org.easymock.EasyMock;
import org.easymock.EasyMockSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;


class DataLoaderTest extends EasyMockSupport {

    private UserRepository userRepository;
    private DoctorRepository doctorRepository;
    private PatientRepository patientRepository;
    private DiagnosisRepository diagnosisRepository;

    private UserService userService;

    private DataLoader dataLoader;

    @BeforeEach
    void setUp() {

        userRepository = mock(UserRepository.class);
        doctorRepository = mock(DoctorRepository.class);
        patientRepository = mock(PatientRepository.class);
        diagnosisRepository = mock(DiagnosisRepository.class);
        userService = new UserService(userRepository);

        dataLoader = new DataLoader(userService,userRepository,doctorRepository,patientRepository,diagnosisRepository);
    }

    @Test
    void testRun_whenDatabaseEmpty_loadsInitialData() {

        EasyMock.expect(userRepository.count()).andReturn(0L);

        Doctor doctor = new Doctor("Javier");
        doctor.setId(1);

        Patient patient = new Patient("Mikel", LocalDate.of(1999, 2, 1));
        patient.setId(1);

        EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.empty());
        EasyMock.expect(doctorRepository.save(EasyMock.anyObject(Doctor.class))).andReturn(doctor);

        EasyMock.expect(patientRepository.findById(1)).andReturn(Optional.empty());
        EasyMock.expect(patientRepository.save(EasyMock.anyObject(Patient.class))).andReturn(patient);

        EasyMock.expect(userRepository.save(EasyMock.anyObject(User.class))).andReturn(new User()).times(3);

        EasyMock.expect(diagnosisRepository.count()).andReturn(0L);

        Patient patient3 = new Patient("Ekaitz", LocalDate.of(2004, 10, 28));
        patient3.setId(3);

        EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));

        EasyMock.expect(patientRepository.findById(3)).andReturn(Optional.empty());
        EasyMock.expect(patientRepository.save(EasyMock.anyObject(Patient.class))).andReturn(patient3);

        EasyMock.expect(diagnosisRepository.save(EasyMock.anyObject(Diagnosis.class))).andReturn(new Diagnosis());

        replayAll();

        dataLoader.run();

        verifyAll();
    }

    @Test
    void testRun_whenDatabaseNotEmpty_doesNothing() {

        EasyMock.expect(userRepository.count()).andReturn(5L);
        EasyMock.expect(diagnosisRepository.count()).andReturn(3L);

        replayAll();

        dataLoader.run();

        verifyAll();
    }
}
