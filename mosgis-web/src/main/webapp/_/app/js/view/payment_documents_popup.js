define ([], function () {

    return function (data, view) {

        var callback = $('body').data ('payment_documents_popup.callback')

        $(view).w2uppop ({

            onClose: function () {
                $('body').data('payment_documents_popup.post_data', null)
                callback ($_SESSION.delete('payment_documents_popup.data'))
            }

        }, function () {

            $('#w2ui-popup .container').w2relayout ({

                name: 'popup_layout',

                panels: [
                    {type: 'main', size: 400},
                ],

                onRender: function (e) {
                    $_SESSION.set ('payment_documents_popup.on', 1)
                    use.block ('payment_documents')
                },

            });

        })

    }

})