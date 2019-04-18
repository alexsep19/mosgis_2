define ([], function () {

    $_DO.update_payment_acknowledgment_new = function (e) {
/*    
        var g = w2ui ['new_objects_grid']
        
        var ids = g.getSelection ()
        
        if (!ids.length) die ('foo', 'Вы не выбрали ни одного адреса')
        
        var a = []
        var data = clone ($('body').data ('data'))
        var dt_from = data.item.effectivedate
        var dt_to = data.item.plandatecomptetion

        $.each (ids, function () {

            var r = g.get (this)

            var i = {
                uuid_contract: $_REQUEST.id,
                fiashouseguid: r.fiashouseguid, 
                startdate:     r.startdate,
                enddate:       r.enddate || dt_to,
            }

            if (i.startdate < dt_from) i.startdate = dt_from
            if (i.enddate   > dt_to)   i.enddate = dt_to

            a.push (i)

        })        

        g.lock ()
        var tia = {type: 'contract_objects', action: 'create', id: undefined}
        
        function check () {
            if (!a.length) return reload_page ()
            query (tia, {data: a.pop ()}, check)        
        }
        
        check ()
*/
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