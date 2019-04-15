define ([], function () {

    return function (data, view) {

        var callback = $('body').data ('accounts_popup.callback')

        $(view).w2uppop ({

            onClose: function () {

                if(w2ui['popup_layout']) w2ui['popup_layout'].destroy()

                callback ($_SESSION.delete('accounts_popup.data'))
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