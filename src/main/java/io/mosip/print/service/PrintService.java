package io.mosip.print.service;

import io.mosip.print.dto.CardUpdateRequestDto;
import io.mosip.print.model.EventModel;

public interface PrintService {
    
	/**
	 * Get the card
	 * 
	 * 
	 * @param eventModel
	 * @return
	 * @throws Exception
	 */
	public boolean generateCard(EventModel eventModel) throws Exception;

	public void updateCardStatus(CardUpdateRequestDto cardUpdateInput);
}