package cd.beapi.service.impl;

import cd.beapi.dto.request.CreatePatientRequest;
import cd.beapi.dto.request.SearchPatientRequest;
import cd.beapi.dto.request.UpdatePatientRequest;
import cd.beapi.dto.response.PageData;
import cd.beapi.dto.response.PatientResponse;
import cd.beapi.entity.Patient;
import cd.beapi.entity.QPatient;
import cd.beapi.exception.AppException;
import cd.beapi.mapper.PatientMapper;
import cd.beapi.repository.jpa.PatientRepository;
import cd.beapi.service.PatientService;
import cd.beapi.service.SequenceService;
import cd.beapi.utility.ExcelUtil;
import cd.beapi.utility.StringUtil;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {
    private static final int MAX_AGE_UNDER_SUPERVISION = 14;
    private final PatientRepository patientRepository;
    private final SequenceService sequenceService;
    private final PatientMapper patientMapper;

    @Override
    public PatientResponse findById(Long id) {
        return patientMapper.toPatientResponse(
                patientRepository.findById(id).orElseThrow(
                        () -> new AppException("Cannot find patient with id: " + id, HttpStatus.BAD_REQUEST)
                )
        );
    }

    @Override
    public PageData<PatientResponse> search(SearchPatientRequest searchPatientRequest) {
        QPatient p = QPatient.patient;
        BooleanBuilder whereClause = new BooleanBuilder();
        if (StringUtils.hasText(searchPatientRequest.getCodeKeyword())) {
            whereClause.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", p.code)
                            .like("%" + StringUtil.normalizeKeyword(searchPatientRequest.getCodeKeyword()) + "%")
            );
        }
        if (StringUtils.hasText(searchPatientRequest.getNameKeyword())) {
            whereClause.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", p.fullName)
                            .like("%" + StringUtil.normalizeKeyword(searchPatientRequest.getNameKeyword()) + "%")
            );
        }
        if (StringUtils.hasText(searchPatientRequest.getPhoneKeyword())) {
            whereClause.and(p.phone.like("%" + searchPatientRequest.getPhoneKeyword() + "%"));
        }
        if (StringUtils.hasText(searchPatientRequest.getGuardianNameKeyword())) {
            whereClause.and(
                    Expressions.stringTemplate("cast(ai_ci({0}) as string)", p.guardianName)
                            .like("%" + StringUtil.normalizeKeyword(searchPatientRequest.getGuardianNameKeyword()) + "%")
            );
        }
        if (StringUtils.hasText(searchPatientRequest.getGuardianPhoneKeyword())) {
            whereClause.and(p.guardianPhone.like("%" + searchPatientRequest.getGuardianPhoneKeyword() + "%"));
        }
        Sort sort = switch (searchPatientRequest.getSortBy()) {
            case null -> Sort.by("createdAt").descending();
            case NAME -> Sort.by("fullName");
            case NAME_DESC -> Sort.by("fullName").descending();
            case CREATED_AT -> Sort.by("createdAt");
            case CREATED_AT_DESC -> Sort.by("createdAt").descending();
        };
        Pageable pageable = PageRequest.of(searchPatientRequest.getPage(), searchPatientRequest.getSize(), sort);
        Page<Patient> pages = patientRepository.findAll(whereClause, pageable);
        return new PageData<>(
                patientMapper.toPatientResponses(pages.getContent()),
                pages.getNumber(),
                pages.getSize(),
                pages.getTotalElements(),
                pages.getTotalPages()

        );
    }

    @Transactional
    @Override
    public PatientResponse save(CreatePatientRequest createPatientRequest) {
        Patient newPatient = Patient.builder()
                .fullName(createPatientRequest.getFullName())
                .dob(createPatientRequest.getDob())
                .gender(createPatientRequest.getGender())
                .address(createPatientRequest.getAddress())
                .build();
        if (StringUtils.hasText(createPatientRequest.getCode())) {
            if (patientRepository.existsByCode(createPatientRequest.getCode())) {
                throw new AppException("This patient code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            newPatient.setCode(createPatientRequest.getCode());
        } else {
            newPatient.setCode(sequenceService.generatePatientCode());
        }
        long age = ChronoUnit.YEARS.between(createPatientRequest.getDob(), LocalDate.now());
        if (age < MAX_AGE_UNDER_SUPERVISION) {
            if (StringUtils.hasText(createPatientRequest.getGuardianName()) && StringUtils.hasText(createPatientRequest.getGuardianPhone())) {
                newPatient.setGuardianName(createPatientRequest.getGuardianName());
                newPatient.setGuardianPhone(createPatientRequest.getGuardianPhone());
            } else {
                throw new AppException("Child under 14 must have guardian information", HttpStatus.BAD_REQUEST);
            }
        } else {
            if (StringUtils.hasText(createPatientRequest.getPhone())) {
                newPatient.setPhone(createPatientRequest.getPhone());
            } else {
                throw new AppException("Phone is required", HttpStatus.BAD_REQUEST);
            }
        }
        Patient savedPatient = patientRepository.save(newPatient);
        return patientMapper.toPatientResponse(savedPatient);
    }

    @Transactional
    @Override
    public PatientResponse update(Long id, UpdatePatientRequest updatePatientRequest) {
        Patient existedPatient = patientRepository.findById(id).orElseThrow(
                () -> new AppException("Cannot find patient with id: " + id, HttpStatus.BAD_REQUEST));

        // Cập nhật code nếu có thay đổi
        if (StringUtils.hasText(updatePatientRequest.getCode())
            && !updatePatientRequest.getCode().equals(existedPatient.getCode())) {
            if (patientRepository.existsByCode(updatePatientRequest.getCode())) {
                throw new AppException("This patient code has been used, please try another one", HttpStatus.BAD_REQUEST);
            }
            existedPatient.setCode(updatePatientRequest.getCode());
        }

        existedPatient.setFullName(updatePatientRequest.getFullName());
        existedPatient.setDob(updatePatientRequest.getDob());
        existedPatient.setGender(updatePatientRequest.getGender());
        existedPatient.setAddress(updatePatientRequest.getAddress());
        existedPatient.setVersion(updatePatientRequest.getVersion());

        long age = ChronoUnit.YEARS.between(updatePatientRequest.getDob(), LocalDate.now());
        if (age < MAX_AGE_UNDER_SUPERVISION) {
            if (StringUtils.hasText(updatePatientRequest.getGuardianName())
                && StringUtils.hasText(updatePatientRequest.getGuardianPhone())) {
                existedPatient.setGuardianName(updatePatientRequest.getGuardianName());
                existedPatient.setGuardianPhone(updatePatientRequest.getGuardianPhone());
                existedPatient.setPhone(null); // trẻ em không cần phone
            } else {
                throw new AppException("Child under 14 must have guardian information", HttpStatus.BAD_REQUEST);
            }
        } else {
            if (StringUtils.hasText(updatePatientRequest.getPhone())) {
                existedPatient.setPhone(updatePatientRequest.getPhone());
                existedPatient.setGuardianName(null); // đã đủ tuổi, xóa thông tin người giám hộ
                existedPatient.setGuardianPhone(null);
            } else {
                throw new AppException("Phone is required", HttpStatus.BAD_REQUEST);
            }
        }

        Patient savedPatient = patientRepository.save(existedPatient);
        return patientMapper.toPatientResponse(savedPatient);
    }

    @Override
    public void delete(Long id) {
        patientRepository.deleteById(id);
    }

    @Override
    public Resource exportExcel() {
        List<Patient> patients = patientRepository.findAll(Sort.by("createdAt").descending());

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Danh sách bệnh nhân");
            CellStyle headerStyle = ExcelUtil.createHeaderStyle(wb);
            CellStyle dataStyle = ExcelUtil.createDataStyle(wb);
            CellStyle centerStyle = ExcelUtil.createDataCenterStyle(wb);

            String[] headers = {"STT", "Mã BN", "Họ tên", "Ngày sinh", "Giới tính", "Số điện thoại",
                    "Địa chỉ", "Người giám hộ", "SĐT người giám hộ", "Ngày tạo"};
            ExcelUtil.writeHeader(sheet, headerStyle, headers);

            for (int i = 0; i < patients.size(); i++) {
                Patient patient = patients.get(i);
                Row row = sheet.createRow(i + 1);
                row.setHeightInPoints(18);

                Object[] values = {
                        i + 1,
                        patient.getCode(),
                        patient.getFullName(),
                        patient.getDob(),
                        patient.getGender() == null ? "" : (patient.getGender() ? "Nam" : "Nữ"),
                        patient.getPhone(),
                        patient.getAddress(),
                        patient.getGuardianName(),
                        patient.getGuardianPhone(),
                        patient.getCreatedAt()
                };

                for (int j = 0; j < values.length; j++) {
                    Cell cell = row.createCell(j);
                    ExcelUtil.setCellValue(cell, values[j]);
                    cell.setCellStyle((j == 0 || j == 4) ? centerStyle : dataStyle);
                }
            }

            ExcelUtil.autoSizeColumns(sheet, headers.length);
            wb.write(out);
            return new ByteArrayResource(out.toByteArray());
        } catch (IOException e) {
            throw new AppException("Cannot export patient data to Excel", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
