package io.mosip.print.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.print.entity.CardDetail;
import io.mosip.print.entity.NotificationStatus;
import io.mosip.print.repository.CardDetailRepository;
import io.mosip.print.repository.NotificationStatusRepository;

@Component
public class CardDetailDao {

	@Autowired
    private CardDetailRepository cardDetailRepository;
	
	@Autowired
	private NotificationStatusRepository notificationStatusRepository;
	
	@Transactional
	public List<CardDetail> fetchUnsentRecords(int fetchSize) {
	    List<CardDetail> records = cardDetailRepository.getUnsendRecords(fetchSize);

	    List<String> ids = records.stream().map(CardDetail::getTransactionId).collect(Collectors.toList());

	    if (!ids.isEmpty()) {
	        cardDetailRepository.markAsProcessing(ids);
	    }

	    return records;
	}

	@Transactional
	public List<CardDetail> fetchEnrollmentFailedRecords(int fetchSize) {
	    List<CardDetail> records = cardDetailRepository.getEnrollmentFailedRecords(fetchSize);

	    List<String> ids = records.stream().map(CardDetail::getTransactionId).collect(Collectors.toList());

	    if (!ids.isEmpty()) {
	        cardDetailRepository.markAsProcessing(ids);
	    }

	    return records;
	}
	
	@Transactional
	public List<NotificationStatus> fetchUnnotifiedRecords(int fetchSize) {
	    List<NotificationStatus> records = notificationStatusRepository.getUnnotifiedRecords(fetchSize);

	    for (NotificationStatus record : records) {
            record.setIsProcessing(true);
        }

	    notificationStatusRepository.saveAll(records);

	    return records;
	}

	@Transactional
	public int resetStuckCardDetailRecords() {
	    return cardDetailRepository.resetStuckCardDetailRecords();
	}

	@Transactional
	public List<CardDetail> fetchCardDetailByRegId(String regId) {
		return cardDetailRepository.findByRegId(regId);
	}

	@Transactional
	public List<CardDetail> fetchCardDetailByNin(String nin) {
		return cardDetailRepository.findByNin(nin);
	}
}
