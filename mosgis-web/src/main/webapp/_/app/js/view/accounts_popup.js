define ([], function () {

    return function (data, view) {

        var callback = $('body').data ('accounts_popup.callback')

        $(view).w2uppop ({

            onClose: function () {
                $('body').data('accounts_popup.post_data', null)
                callback ($_SESSION.delete ('accounts_popup.on'))
            }

        }, function () {

            $('#w2ui-popup .container').w2relayout ({

                name: 'popup_layout',

                panels: [
                    {type: 'main', size: 400},
                ],

                onRender: function (e) {
                    $_SESSION.set ('accounts_popup.on', 1)
                    use.block ('accounts')
                },

            });

        })

    }

})