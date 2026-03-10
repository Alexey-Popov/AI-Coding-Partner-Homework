/**
 * User API Routes
 * Defines REST endpoints for user management
 */

const express = require('express');
const router = express.Router();
const userController = require('../controllers/userController');

// Get all users
router.get('/api/users', userController.getAllUsers);

// Get single user by ID
router.get('/api/users/:id', userController.getUserById);

module.exports = router;
