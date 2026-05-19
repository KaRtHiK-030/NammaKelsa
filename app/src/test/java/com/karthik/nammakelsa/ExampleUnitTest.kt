package com.karthik.nammakelsa

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun worker_validation_is_correct() {
        val worker = Worker(
            userId = "123",
            name = "Karthik",
            skill = "Electrician",
            location = "Bangalore",
            chargePerDay = 1200
        )

        assertTrue(worker.isValid)
        assertEquals("₹1200/day", worker.displayChargeText)
        assertEquals("Electrician", worker.displaySkill)
        assertTrue(worker.isAvailable)
    }

    @Test
    fun request_status_helpers_work() {
        val request = Request(
            requestId = "req1",
            workerId = "worker1",
            hirerId = "hirer1",
            workDetails = "Need wiring work",
            status = RequestStatus.PENDING
        )

        assertTrue(request.isPending)
        assertFalse(request.isAccepted)
        assertEquals("Pending", request.statusLabel)
    }

    @Test
    fun review_rating_safety_works() {
        val review = Review(
            reviewId = "rev1",
            workerId = "worker1",
            userId = "user1",
            reviewerName = "John",
            rating = 8f
        )

        assertEquals(5f, review.safeRating)
        assertTrue(review.isValid)
    }

    @Test
    fun message_validation_works() {
        val message = Message(
            senderId = "u1",
            receiverId = "u2",
            message = "Hello"
        )

        assertTrue(message.isValid)
        assertTrue(message.isTextMessage)
        assertEquals("Hello", message.previewText)
    }

    @Test
    fun image_message_preview_works() {
        val message = Message(
            senderId = "u1",
            receiverId = "u2",
            imageUrl = "https://image.com/pic.jpg",
            type = MessageType.IMAGE
        )

        assertTrue(message.isImageMessage)
        assertEquals("📷 Image", message.previewText)
    }

    @Test
    fun worker_skill_fallback_works() {
        val worker = Worker(
            userId = "1",
            name = "Alex",
            skillsList = listOf(
                WorkerSkill(
                    name = "Plumber",
                    chargePerDay = 900
                )
            )
        )

        assertEquals("Plumber", worker.displaySkill)
    }
}