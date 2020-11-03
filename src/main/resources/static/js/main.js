

$(document).ready(function () {
    $('.table .editUserModal').on('click',function (event) {
        event.preventDefault();

        var href = $(this).attr('href');
        $.get(href, function (user, status) {
            $('#idEdit').val(user.id);
            $('#usernameEdit').val(user.id);
            $('#firstNameEdit').val(user.id);
            $('#lastNameEdit').val(user.id);
            $('#ageEdit').val(user.id);
            $('#emailEdit').val(user.id);

        })

        $('#editModal').modal();
    });


})