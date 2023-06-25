package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest.Type.*;

public class TicketServiceImpl implements TicketService {

    private static final int MAXIMUM_TICKETS = 20;
    private static final int ADULT_TICKET_PRICE = 20;
    private static final int CHILD_TICKET_PRICE = 10;
    private final TicketPaymentService paymentService;
    private final SeatReservationService seatReservationService;

    public TicketServiceImpl(TicketPaymentService paymentService, SeatReservationService seatReservationService) {
        this.paymentService = paymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        if (isValidPurchaseRequest(accountId, ticketTypeRequests)) {
            paymentService.makePayment(accountId, getTotalPriceFrom(ticketTypeRequests));
            seatReservationService.reserveSeat(accountId, getTotalSeatsFrom(ticketTypeRequests));
        } else throw new InvalidPurchaseException();
    }

    private boolean isValidPurchaseRequest(long accountId, TicketTypeRequest... requests) {
        if (requests == null || requests.length == 0) return false; // null check for requests
        int nTickets = 0;
        boolean isAdult = false;
        for (TicketTypeRequest request : requests) {
            nTickets += request.getNoOfTickets();
            if (request.getTicketType() == ADULT) isAdult = true;
        }
        return (accountId > 0) && (nTickets <= MAXIMUM_TICKETS) && isAdult;
    }

    private int getTotalPriceFrom(TicketTypeRequest... requests) {
        int price = 0;
        for (TicketTypeRequest request : requests) {
            if (request.getTicketType() == ADULT) price += request.getNoOfTickets() * ADULT_TICKET_PRICE;
            if (request.getTicketType() == CHILD) price += request.getNoOfTickets() * CHILD_TICKET_PRICE;
        }
        return price;
    }

    private int getTotalSeatsFrom(TicketTypeRequest... requests) {
        int seats = 0;
        for (TicketTypeRequest request : requests) {
            if (request.getTicketType() != INFANT) seats += request.getNoOfTickets();
        }
        return seats;
    }

}
