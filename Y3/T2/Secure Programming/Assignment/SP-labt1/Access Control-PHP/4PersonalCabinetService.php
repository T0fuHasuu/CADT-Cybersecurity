<?php

namespace App\services\profile;

use App\models\Report;
use App\models\User;
use App\services\check\CheckDataService;
use App\services\import\Archiver;
use App\services\import\XMLHandler;
use Framework\HTTP\Request;

class PersonalCabinetService
{
    /**
     * @var CheckDataService
     */
    private CheckDataService $service;

    public function __construct()
    {
        $this->service = new CheckDataService();
    }

    /**
     * @param string $hashKey
     * @param Request $request
     * @return bool
     */
    public function dropUser(string $hashKey, Request $request): bool
    {
        $post = $request->getParsedBody();
        $sanitized = $this->service->sanitize($post);

        if ($sanitized['email'] !== null
            && $this->service->isValidEmail($sanitized['email'])
            && User::getCurrentUser($hashKey)->role === User::ADMINISTRATOR
        ) {
            return User::dropUser($hashKey, $sanitized['email']);
        }

        return false;
    }

    /**
     * @param string $hashKey
     * @param Request $request
     * @return bool
     */
    public function uploadReport(string $hashKey, Request $request): bool
    {
        $user = User::getCurrentUser($hashKey);
        $userDir = $this->service->getUserDir($user);
        $name = basename($_FILES['xml']['name'] . '-' . $user->uid);

        if ($userDir && $user->role === User::MANAGER) {
            $xmlHandler = new XMLHandler($request);

            if (move_uploaded_file($name, $userDir . 'reports')) {
                return Report::saveReport($hashKey, $xmlHandler->handle());
            }
        }

        return false;
    }

    /**
     * @param string $hashKey
     * @return false|mixed
     */
    public function getReports(string $hashKey)
    {
        if (User::getCurrentUser($hashKey)->role === User::MANAGER) {
            return Report::getReports($hashKey);
        }

        return false;
    }

    /**
     * @param string $hashKey
     * @param Request $request
     * @return bool
     */
    public function deleteReport(string $hashKey, Request $request): bool
    {
        $post = $request->getParsedBody();
        $sanitized = $this->service->sanitize($post);

        if (User::getCurrentUser($hashKey)->role === User::MANAGER) {
            return Report::deleteReport($hashKey, $sanitized['report_id']);
        }

        return false;
    }

    /**
     * @param string $hashKey
     * @return bool
     */
    public function archiveReports(string $hashKey): bool
    {
        $user = User::getCurrentUser($hashKey);

        if ($user->role === User::MANAGER && $this->service->isValidPath($hashKey)) {
            $userDir = $this->service->getUserDir($user);
            $reports = scandir($userDir . 'reports');

            return Archiver::create($hashKey, $reports);
        }

        return false;
    }
}
