
define ([], function () {

    return function (data, view) {

        var topmost_layout = w2ui ['topmost_layout']

        topmost_layout.unlock ('main')

        $(topmost_layout.el ('main')).w2relayout ({

            name: 'service_payments_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'accounts', caption: 'Лицевые счета'
                                , off: false
                            },
                            {id: 'payment_documents', caption: 'Платежные документы'
                                , off: false
                            },
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_service_payments

                    }

                },

            ],

            onRender: function (e) {
                this.get ('main').tabs.click (data.active_tab)
            },

        });

    }

});