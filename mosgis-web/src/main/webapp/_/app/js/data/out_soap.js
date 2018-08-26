define ([], function () {

    $_DO.show_out_soap = function (e) {
        
        var name = w2ui ['out_soap_layout'].get ('main').tabs.active
        
        if (/^out_soap_[0-9]$/.test (name)) return 
        
        w2ui [name + '_grid'].lock ()

        use.block (name)
        
    }

    $_DO.choose_tab_out_soap = function (e) {

        var name = e.tab.id

        localStorage.setItem ('out_soap.active_tab', name)
        
        if (!/^out_soap_[0-9]$/.test (name)) use.block (name)

    }    
    
    function month (i) {
        return {id: (i < 10 ? '0' : '') + new String (i), text: w2utils.settings.fullmonths [i - 1]}
    }

    return function (done) {

        var data = {months: [], years: []}
        
        for (var i = 0; i < 11; i ++) data.months.push (month (i + 1))
        
        var now = new Date ()
        
        data.record = {
            is_failed: 0,
            year: {id: now.getFullYear ()},
            month: [month (1 + now.getMonth ())]
        }

        for (var i = 2018; i <= data.record.year.id; i ++) data.years.push ({id: i, text: i})

        data.active_tab = localStorage.getItem ('out_soap.active_tab') || 'out_soap_0'             

        done (data)

    }

})