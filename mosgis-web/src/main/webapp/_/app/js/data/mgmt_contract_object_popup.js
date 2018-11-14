define ([], function () {

    $_DO.update_mgmt_contract_object_popup = function (e) {
    
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

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = {}
        
        var idx = {}; $.each (w2ui ['mgmt_contract_objects_grid'].records, function () {idx [this.fiashouseguid] = 1})
        
        query ({type: 'charter_objects', id: undefined}, {search:[{field: "uuid_charter", operator: "is", value: data.item ['ch.uuid']}], offset: 0, limit: 100000}, function (d) {
 
            var a = []
            
            $.each (d.root, function () {
                if (!this.ismanagedbycontract || idx [this.fiashouseguid]) return
                this.label = this ['fias.label']
                this.recid = this.uuid
                a.push (this)
            })
            
            if (!a.length) die ('foo', 'В уставе организации-заказчика нет подходящих объектов')
            
            data.records = a
 
            done (data)

        })            

    }

})