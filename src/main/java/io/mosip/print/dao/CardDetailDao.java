package io.mosip.print.dao;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.mosip.print.entity.CardDetail;
import io.mosip.print.repository.CardDetailRepository;

@Component
public class CardDetailDao {

	@Autowired
    private CardDetailRepository cardDetailRepository;
	
	@Transactional
	public List<CardDetail> fetchUnsentRecords(int fetchSize) {
	    List<CardDetail> records = cardDetailRepository.getUnsendRecords(fetchSize);

	    List<String> ids = records.stream().map(CardDetail::getTransactionId).collect(Collectors.toList());

	    if (!ids.isEmpty()) {
	        cardDetailRepository.markAsProcessing(ids);
	    }

	    return records;
	}
}
