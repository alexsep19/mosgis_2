define ([], function () {

    return function (data, view) {

        var it = data.item

        it.customer_label = it ['ind_customer.label'] || it ['org_customer.label']

        var acct_items = data.acct_items

        if (acct_items && acct_items.length) {
            it.address_label = ', ' + acct_items [0] ['addr.label']
            it.uuid_house = acct_items [0] ['house.uuid']

            $.each (acct_items, function () {
                var l = this ['prem.label']
                if (l) it.address_label += ', ' + l
            })

        }
        
        var dt_period = new Date (it ['pd.dt_period'])
        it.year = dt_period.getFullYear ()
        it.month_label = w2utils.settings.fullmonths [dt_period.getMonth ()].toLowerCase ()

        $('title').text ('Квитирование: платёж ' + it ['pay.ordernum'])

        fill (view, it, $('#body'))

        $('#container').w2relayout ({

            name: 'topmost_layout',

            panels: [

                {type: 'main', size: 400,

                    tabs: {

                        tabs: [
                            {id: 'acknowledgment_common', caption: 'Общие'},
                        ].filter (not_off),

                        onClick: $_DO.choose_tab_acknowledgment

                    }

                },

            ],

            onRender: function (e) {

                clickActiveTab (this.get ('main').tabs, 'payment.active_tab')

                clickOn ($('#payment_link'), function () { openTab ('/payment/' + it.uuid_pay) })
                clickOn ($('#account_link'), function () { openTab ('/account/' + it ['pay.uuid_account']) })
                clickOn ($('#pay_doc_link'), function () { openTab ('/payment_document/' + it.uuid_pay_doc) })
                
            },

        });

    }

})