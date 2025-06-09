package com.loopmarket.domain.report.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.loopmarket.domain.member.MemberEntity;
import com.loopmarket.domain.member.MemberRepository;
import com.loopmarket.domain.product.entity.ProductEntity;
import com.loopmarket.domain.product.repository.ProductRepository;
import com.loopmarket.domain.report.dto.ReportDTO;
import com.loopmarket.domain.report.entity.ReportEntity;
import com.loopmarket.domain.report.repository.ReportRepository;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

    public ReportService(ReportRepository reportRepository, ProductRepository productRepository, MemberRepository memberRepository) {
        this.reportRepository = reportRepository;
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
    }
    
    @Transactional
    public void saveReport(ReportDTO reportDTO) {
        ReportEntity entity = new ReportEntity();
        entity.setReporterId(reportDTO.getReporterId());
        entity.setReportedUserId(reportDTO.getReportedUserId());
        entity.setProductId(reportDTO.getProductId());
        entity.setReason(reportDTO.getReason());
        entity.setDetail(reportDTO.getDetail());

        reportRepository.save(entity);
    }
    
    @Transactional
    public List<ReportDTO> findReportsByReporter(int reporterId) {
        List<ReportEntity> reports = reportRepository.findByReporterId(reporterId);
        return reports.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public ReportDTO convertToDTO(ReportEntity entity) {
        ReportDTO dto = new ReportDTO();
        dto.setReportId(entity.getReportId());
        dto.setReporterId(entity.getReporterId());
        dto.setReportedUserId(entity.getReportedUserId());
        dto.setProductId(entity.getProductId());
        dto.setReason(entity.getReason());
        dto.setDetail(entity.getDetail());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setProcessed(entity.isProcessed());
        
        dto.setResponseMessage(entity.getResponseMessage());  // 이 부분 추가!

        // 상품명 조회
        if (entity.getProductId() != null) {
            productRepository.findById(entity.getProductId().longValue())
                .ifPresentOrElse(
                    product -> dto.setProductTitle(product.getTitle()),
                    () -> dto.setProductTitle("상품명 없음")
                );
        } else {
            dto.setProductTitle("상품명 없음");
        }

        // 신고자 닉네임 조회
        if (entity.getReporterId() != null) {
            memberRepository.findNicknameByUserId(entity.getReporterId())
                .ifPresentOrElse(
                    dto::setNickname,
                    () -> dto.setNickname("닉네임 없음")
                );
        } else {
            dto.setNickname("닉네임 없음");
        }

        return dto;
    }
    
    public ReportDTO findById(Long id) {
        Optional<ReportEntity> optionalEntity = reportRepository.findById(id);
        if (optionalEntity.isPresent()) {
            return convertToDTO(optionalEntity.get());
        } else {
            throw new RuntimeException("Report not found with id: " + id);
        }
    }
    
    @Transactional
    public void submitReply(Long reportId, String reply) {
        ReportEntity report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("신고 내역을 찾을 수 없습니다."));

        report.setResponseMessage(reply);
        report.setProcessed(true);
        reportRepository.save(report);
    }
    
    
    
}