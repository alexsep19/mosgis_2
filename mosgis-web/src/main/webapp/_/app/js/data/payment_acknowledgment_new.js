define ([], function () {

    $_DO.update_payment_acknowledgment_new = function (e) {
    
        var g = w2ui ['new_objects_grid']
        
        var ids = g.getSelection ()
        
        if (!ids.length) die ('foo', 'Укажите, пожалуйста, платёжный документ')
        
        var r = g.get (ids [0])
        
        query ({type: 'acknowledgments', action: 'create', id: null}, {data: {
        
            uuid_pay     : $_REQUEST.id,           
            uuid_pay_doc : r.uuid,
            amount       : r.amount_nack
            
        }}, function (data) {

            w2popup.close ()
            
            if (data.id) w2confirm ('Перейти на страницу квитирования?').yes (function () {openTab ('/acknowledgment/' + data.id)})
            
            var grid = w2ui ['payment_acknowledgments_grid']

            grid.reload (grid.refresh)
            
        })
    
    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var it = data.item
        
        data.accounts = [{id: it.uuid_account, text: it ['acct.accountnumber']}]

        var periods = []
        
        var dt = new Date ()
        dt.setDate (1)
                
        while (periods.length < 36) {
        
            periods.push ({
                id: dt.toJSON ().slice (0, 7) + '-01',
                text: w2utils.settings.fullmonths [dt.getMonth ()] + ' ' + dt.getFullYear ()
            })
            
            dt.setMonth (dt.getMonth () - 1)

        }        
        
        data.periods = periods

        done (data)

    }

})