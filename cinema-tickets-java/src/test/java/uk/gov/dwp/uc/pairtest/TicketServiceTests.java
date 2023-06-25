package uk.gov.dwp.uc.pairtest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketServiceTests {

    private final TicketTypeRequest[] invalidRequests1 = new TicketTypeRequest[3];
    private final TicketTypeRequest[] invalidRequests2 = new TicketTypeRequest[2];
    private TicketTypeRequest[] invalidRequests3;
    private final TicketTypeRequest[] validRequests = new TicketTypeRequest[3];
    private long invalidAccountId;
    private long validAccountId;
    @Mock
    private TicketPaymentService paymentService;
    @Mock
    private SeatReservationService seatReservationService;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Before
    public void setup() {
        //more than MAXIMUM_TICKETS
        invalidRequests1[0] = new TicketTypeRequest(CHILD, 10);
        invalidRequests1[1] = new TicketTypeRequest(INFANT, 9);
        invalidRequests1[2] = new TicketTypeRequest(ADULT, 2);

        //no Adult ticket, only Child & Infant
        invalidRequests2[0] = new TicketTypeRequest(CHILD, 2);
        invalidRequests2[1] = new TicketTypeRequest(INFANT, 2);

        //null request
        invalidRequests3 = null;

        //valid - at least one adult , total tickets < MAXIMUM_TICKETS
        validRequests[0] = new TicketTypeRequest(ADULT, 1);
        validRequests[1] = new TicketTypeRequest(CHILD, 1);
        validRequests[2] = new TicketTypeRequest(INFANT, 1);

        invalidAccountId = -12345;
        validAccountId = 12345;

        MockitoAnnotations.openMocks(this);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void whenMoreThanMaximumTickets_thenThrowInvalidPurchaseException() {
        ticketService.purchaseTickets(validAccountId, invalidRequests1);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void whenNoAdultTicket_thenThrowInvalidPurchaseException() {
        ticketService.purchaseTickets(validAccountId, invalidRequests2);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void whenNullTicketRequest_thenThrowInvalidPurchaseException() {
        ticketService.purchaseTickets(validAccountId, invalidRequests3);
    }

    @Test(expected = InvalidPurchaseException.class)
    public void whenInvalidAccountId_thenThrowInvalidPurchaseException() {
        ticketService.purchaseTickets(invalidAccountId, validRequests);
    }

    @Test //Happy-Path
    public void whenValidRequest_thenVerifyHappyPath() {
        ticketService.purchaseTickets(validAccountId, validRequests);
        //verify dependent service invocation times with calculated amount and seats as arguments
        verify(paymentService, times(1)).makePayment(validAccountId, 30); //Adult(20) + Child(10)
        verify(seatReservationService, times(1)).reserveSeat(validAccountId, 2); //Adult(1) + Child(1)
    }

}
