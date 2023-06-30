import express from 'express';
const router = express.Router();

import * as addressController from '../controllers/addressController'

router.get('/', addressController.getAddresses )
router.post('/', addressController.postAddress )

export default router;