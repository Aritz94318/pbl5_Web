 package edu.mondragon.we2.pinkAlert.service;
 import edu.mondragon.we2.pinkAlert.model.Doctor;
 import edu.mondragon.we2.pinkAlert.repository.DoctorRepository;
 import org.easymock.EasyMock;
 import org.easymock.EasyMockSupport;
 import org.junit.jupiter.api.BeforeEach;
 import org.junit.jupiter.api.Test;
 import java.util.List;
 import java.util.Optional;
 import static org.junit.jupiter.api.Assertions.*;
 class DoctorServiceTest extends EasyMockSupport {
     private DoctorRepository doctorRepository;
     private DoctorService doctorService;
     private Doctor doctor;
     @BeforeEach
     void setUp() {
         doctorRepository = mock(DoctorRepository.class);
         doctorService = new DoctorService(doctorRepository);
         doctor = new Doctor();
         doctor.setId(1);
     }
     @Test
     void testFindAll() {
         EasyMock.expect(doctorRepository.findAll()).andReturn(List.of(doctor));
         EasyMock.replay(doctorRepository);
         List<Doctor> result = doctorService.findAll();
         assertEquals(1, result.size());
         assertEquals(doctor, result.get(0));
         EasyMock.verify(doctorRepository);
     }
     @Test
     void testFindById() {
         EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));
         EasyMock.replay(doctorRepository);
         Doctor result = doctorService.findById(1);
         assertNotNull(result);
         EasyMock.verify(doctorRepository);
     }
     @Test
     void testFindByIdNotFound() {
         EasyMock.expect(doctorRepository.findById(99)).andReturn(Optional.empty());
         EasyMock.replay(doctorRepository);
         RuntimeException ex = assertThrows(RuntimeException.class,() -> doctorService.findById(99)
         );
         assertTrue(ex.getMessage().contains("Doctor not found"));
         EasyMock.verify(doctorRepository);
     }
     @Test
     void testCreateDoctor() {
         EasyMock.expect(doctorRepository.save(EasyMock.anyObject(Doctor.class))).andAnswer(() -> {
                     Doctor d = (Doctor) EasyMock.getCurrentArguments()[0];
                     d.setId(1);
                     return d;
                 });
         EasyMock.replay(doctorRepository);
         Doctor result = doctorService.create(doctor);
         assertNotNull(result);
         assertEquals(1, result.getId());
         EasyMock.verify(doctorRepository);
     }
     @Test
     void testUpdateDoctor() {
         Doctor updated = new Doctor();
         EasyMock.expect(doctorRepository.findById(1)).andReturn(Optional.of(doctor));
         EasyMock.expect(doctorRepository.save(doctor))
                 .andReturn(doctor);
         EasyMock.replay(doctorRepository);
         Doctor result = doctorService.update(1, updated);
         EasyMock.verify(doctorRepository);
     }
     @Test
     void testDeleteDoctor() {
         doctorRepository.deleteById(1);
         EasyMock.expectLastCall().once();
         EasyMock.replay(doctorRepository);
         doctorService.delete(1);
         EasyMock.verify(doctorRepository);
     }
 }
