package bigcie.bigcie.scripts;

import bigcie.bigcie.entities.*;
import bigcie.bigcie.entities.enums.BikeType;
import bigcie.bigcie.entities.enums.PricingPlan;
import bigcie.bigcie.entities.enums.ReservationStatus;
import bigcie.bigcie.entities.enums.TripStatus;
import bigcie.bigcie.repositories.ReservationRepository;
import bigcie.bigcie.repositories.TripRepository;
import bigcie.bigcie.repositories.UserRepository;
import bigcie.bigcie.services.interfaces.IBikeService;
import bigcie.bigcie.services.interfaces.IBikeStationService;
import bigcie.bigcie.services.interfaces.IReservationService;
import bigcie.bigcie.services.interfaces.IUserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Component
public class Script implements CommandLineRunner {
    private final IBikeStationService bikeStationService;
    private final IBikeService bikeService;
    private final TripRepository tripRepository;
    private final IUserService userService;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;

    public Script(IBikeStationService bikeStationService, IBikeService bikeService, IReservationService reservationService, TripRepository tripRepository, IUserService userService, UserRepository userRepository, ReservationRepository reservationRepository) {
        this.bikeStationService = bikeStationService;
        this.bikeService = bikeService;
        this.tripRepository = tripRepository;
        this.userService = userService;
        this.userRepository = userRepository;
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // //
        // clearBikeFromUser(UUID.fromString("93e21f50-5bf8-4891-9d3a-30a9676f3b36"));
        // setStationStatus();
        recountAllStations();
//        reservationPopulate();
//        defaulttobronze();
//        bronzeToSilver();
        silverToGold();
    }

    private void recountAllStations() {
        bikeStationService.getAllStations().forEach(station -> {
            int standardBikes = 0;
            int eBikes = 0;

            for (UUID bikeId : station.getBikesIds()) {
                if (bikeService.getBikeById(bikeId).getBikeType() == BikeType.STANDARD) {
                    standardBikes++;
                } else {
                    eBikes++;
                }
            }

            station.setStandardBikesDocked(standardBikes);
            station.setEBikesDocked(eBikes);
            bikeStationService.updateStation(station.getId(), station);
        });
    }
//    private void defaulttobronze() {
//        BikeStation bikeStation = bikeStationService.getAllStations().getFirst();
//        Bike bike = bikeService.getBikeById(bikeStation.getBikesIds().get(0));
//        User user = userService.getUserByUUID(userRepository.findByEmail("defaulttobronze@test.com").get().getId());
//        Rider rider = (Rider)  user;
//
//        List<Trip> trips = tripRepository.findByUserId(user.getId());
//        for (int i = 0; i < 9; i++) {
//            Trip trip = new Trip.Builder()
//                    .bikeId(bike.getId())
//                    .bikeStationStartId(bikeStation.getId())
//                    .bikeStationEndId(bikeStation.getId())
//                    .startDate(LocalDateTime.now().minusMonths(2))
//                    .endDate(LocalDateTime.now().minusMonths(2).plusMinutes(1))
//                    .status(TripStatus.COMPLETED)
//                    .distanceInKm(0.0)
//                    .bikeType(bike.getBikeType())
//                    .pricingPlan(PricingPlan.SINGLE_RIDE)
//                    .discountApplied(0)
//                    .userId(user.getId())
//                    .paymentInfoId(rider.getDefaultPaymentInfo().getId())
//                    .cost(1.19)
//                    .build();
//
//            trips.add(trip);
//        }
//        tripRepository.saveAll(trips);
//
//    }

    public void bronzeToSilver() {
        BikeStation bikeStation = bikeStationService.getAllStations().getFirst();
        Bike bike = bikeService.getBikeById(bikeStation.getBikesIds().get(0));
        User user = userService.getUserByUUID(userRepository.findByEmail("silvertogold@test.com").get().getId());
        Rider rider = (Rider)  user;

        List<Trip> trips = tripRepository.findByUserId(user.getId());
        for (int i = 0; i < 5; i++) {
            Trip trip = new Trip.Builder()
                    .bikeId(bike.getId())
                    .bikeStationStartId(bikeStation.getId())
                    .bikeStationEndId(bikeStation.getId())
                    .startDate(LocalDateTime.now().minusMonths(2))
                    .endDate(LocalDateTime.now().minusMonths(2).plusMinutes(1))
                    .status(TripStatus.COMPLETED)
                    .distanceInKm(0.0)
                    .bikeType(bike.getBikeType())
                    .pricingPlan(PricingPlan.SINGLE_RIDE)
                    .discountApplied(0)
                    .userId(user.getId())
                    .paymentInfoId(rider.getDefaultPaymentInfo().getId())
                    .cost(1.19)
                    .build();

            trips.add(trip);
        }
        for (int i = 0; i < 5; i++) {
            Trip trip = new Trip.Builder()
                    .bikeId(bike.getId())
                    .bikeStationStartId(bikeStation.getId())
                    .bikeStationEndId(bikeStation.getId())
                    .startDate(LocalDateTime.now().minusMonths(1))
                    .endDate(LocalDateTime.now().minusMonths(1).plusMinutes(1))
                    .status(TripStatus.COMPLETED)
                    .distanceInKm(0.0)
                    .bikeType(bike.getBikeType())
                    .pricingPlan(PricingPlan.SINGLE_RIDE)
                    .discountApplied(0)
                    .userId(user.getId())
                    .paymentInfoId(rider.getDefaultPaymentInfo().getId())
                    .cost(1.19)
                    .build();

            trips.add(trip);
        }
        for (int i = 0; i < 5; i++) {
            Trip trip = new Trip.Builder()
                    .bikeId(bike.getId())
                    .bikeStationStartId(bikeStation.getId())
                    .bikeStationEndId(bikeStation.getId())
                    .startDate(LocalDateTime.now().minusMonths(0))
                    .endDate(LocalDateTime.now().minusMonths(0).plusMinutes(1))
                    .status(TripStatus.COMPLETED)
                    .distanceInKm(0.0)
                    .bikeType(bike.getBikeType())
                    .pricingPlan(PricingPlan.SINGLE_RIDE)
                    .discountApplied(0)
                    .userId(user.getId())
                    .paymentInfoId(rider.getDefaultPaymentInfo().getId())
                    .cost(1.19)
                    .build();

            trips.add(trip);
        }
        tripRepository.saveAll(trips);

        List<Reservation> reservations = reservationRepository.findByUserId(rider.getId());
        for (int i = 0; i < 4; i++ ) {
            Reservation reservation = new Reservation(
                    UUID.randomUUID(),
                    rider.getId(),
                    bikeStation.getId(),
                    bike.getId(),
                    LocalDateTime.now(),
                    LocalDateTime.now().plusMinutes(2),
                    ReservationStatus.COMPLETED
            );
            reservations.add(reservation);
        }
        reservationRepository.saveAll(reservations);




    }
    private void silverToGold() {
        BikeStation bikeStation = bikeStationService.getAllStations().getFirst();
        Bike bike = bikeService.getBikeById(bikeStation.getBikesIds().get(0));
        User user = userService.getUserByUUID(userRepository.findByEmail("silvertogold@test.com").get().getId());
        Rider rider = (Rider)  user;
        List<Trip> trips = tripRepository.findByUserId(user.getId());
        for (int week = 0; week < 14; week++) {
            for (int i = 0; i < 5; i++) {
                Trip trip = new Trip.Builder()
                        .bikeId(bike.getId())
                        .bikeStationStartId(bikeStation.getId())
                        .bikeStationEndId(bikeStation.getId())
                        .startDate(LocalDateTime.now().minusWeeks(week))
                        .endDate(LocalDateTime.now().minusWeeks(week).plusMinutes(1))
                        .status(TripStatus.COMPLETED)
                        .distanceInKm(0.0)
                        .bikeType(bike.getBikeType())
                        .pricingPlan(PricingPlan.SINGLE_RIDE)
                        .discountApplied(0)
                        .userId(user.getId())
                        .paymentInfoId(rider.getDefaultPaymentInfo().getId())
                        .cost(1.19)
                        .build();

                trips.add(trip);
            }
        }
        tripRepository.saveAll(trips);
    }
    private void reservationPopulate() {
        BikeStation bikeStation = bikeStationService.getAllStations().getFirst();
        Bike bike = bikeService.getBikeById(bikeStation.getBikesIds().get(0));

//        byte[] bytes = Base64.getDecoder().decode("kUj4W1Af4pM2O29nqTA6nQ==");
//        UUID uuid = fromLegacyMongoUUID(bytes);

        User user = userService.getUserByUUID(userRepository.findByEmail("antho.lim44@gmail.com").get().getId());
        Rider rider = (Rider)  user;

        List<Trip> trips = tripRepository.findByUserId(user.getId());
        for (int i = 0; i < 5; i++) {
            Trip trip = new Trip.Builder()
                    .bikeId(bike.getId())
                    .bikeStationStartId(bikeStation.getId())
                    .bikeStationEndId(bikeStation.getId())
                    .startDate(LocalDateTime.now().minusMonths(2))
                    .endDate(LocalDateTime.now().minusMonths(2).plusMinutes(1))
                    .status(TripStatus.COMPLETED)
                    .distanceInKm(0.0)
                    .bikeType(bike.getBikeType())
                    .pricingPlan(PricingPlan.SINGLE_RIDE)
                    .discountApplied(0)
                    .userId(user.getId())
                    .paymentInfoId(rider.getDefaultPaymentInfo().getId())
                    .cost(1.19)
                    .build();

            trips.add(trip);
        }
        for (int i = 0; i < 5; i++) {
            Trip trip = new Trip.Builder()
                    .bikeId(bike.getId())
                    .bikeStationStartId(bikeStation.getId())
                    .bikeStationEndId(bikeStation.getId())
                    .startDate(LocalDateTime.now().minusMonths(1))
                    .endDate(LocalDateTime.now().minusMonths(1).plusMinutes(1))
                    .status(TripStatus.COMPLETED)
                    .distanceInKm(0.0)
                    .bikeType(bike.getBikeType())
                    .pricingPlan(PricingPlan.SINGLE_RIDE)
                    .discountApplied(0)
                    .userId(user.getId())
                    .paymentInfoId(rider.getDefaultPaymentInfo().getId())
                    .cost(1.19)
                    .build();

            trips.add(trip);
        }
        tripRepository.saveAll(trips);

    }

    public UUID fromLegacyMongoUUID(byte[] legacyBytes) {
        ByteBuffer bb = ByteBuffer.wrap(legacyBytes).order(ByteOrder.BIG_ENDIAN);

        // Extract reversed time fields
        int timeLow = Integer.reverseBytes(bb.getInt());
        short timeMid = Short.reverseBytes(bb.getShort());
        short timeHi = Short.reverseBytes(bb.getShort());

        // Extract normal-order fields
        long msb = ((long) timeLow & 0xffffffffL) << 32
                | ((long) timeMid & 0xffffL) << 16
                | ((long) timeHi & 0xffffL);

        long lsb = bb.getLong();

        return new UUID(msb, lsb);
    }


}
