define ([], function () {

    var form_name = 'interval_object_popup_form'

    $_DO.load_premises_for_interval_object_popup = function (e) {

        var form = w2ui [form_name]
        
        var r = form.record

        var premises = []
        
        function done () {
            form.get ('uuid_premise').options.items = premises
            form.refresh ()
        }

        var fiashouseguid = r.fiashouseguid
            
        if (!fiashouseguid) return done ()
        
        if (fiashouseguid.id) fiashouseguid = fiashouseguid.id
        
        var uuid_house = form.record.f2h [fiashouseguid]

        if (!uuid_house) return done ()
        
        query ({type: 'premises', id: undefined}, {data: {uuid_house: uuid_house}}, function (d) {
        
            for (k in d) premises = d [k].map (function (i) {return {
                id: i.id,
                text: i.label + ' (' + i.totalarea + ' м\xB2)',
            }})
            
            done ()

        })

    }

    $_DO.update_interval_object_popup = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        
        if (!v.fiashouseguid) die ('fiashouseguid', 'Укажите, пожалуйста, адрес')
                
        var tia = {type: 'interval_objects', id: form.record.uuid}
        
        if (tia.id) {
            tia.action = 'update'
        }
        else {
            tia.action = 'create'
            v.uuid_interval = $_REQUEST.id
        }

        query (tia, {data: v}, function (data) {
            w2popup.close ()
            var grid = w2ui ['interval_objects_grid']
            grid.reload (grid.refresh)
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        data.record = $_SESSION.delete ('record') || {
            sharepercent: 100,
        }
                
        var tia = {id: undefined}
        var p   = {limit: 100000, offset:0, search: [{
            field:    "id_ctr_status_gis",
            operator: "is",
            value: 40
        }]}
        
        var it = data.item

        if (it.uuid_sr_ctr) {

            tia.type = 'supply_resource_contract_objects'

            p.search.push({
                field: "uuid_sr_ctr",
                operator: "is",
                value: it.uuid_sr_ctr
            })

        }

        if (it.uuid_contract) {
        
            tia.type = 'contract_objects'
            
            p.search.push ({
                field:    "uuid_contract",
                operator: "is",
                value:    it.uuid_contract
            })
            
        }               
        if (it.uuid_charter) {
        
            tia.type = 'charter_objects'
            
            p.search.push ({
                field:    "uuid_charter",
                operator: "is",
                value:    it.uuid_charter
            })
            
        }   
        
        data.fias = []
        
        query (tia, p, function (d) {
        
            var f2h = {}

            $.each (d.root, function () {
            
                var id = this.fiashouseguid            
                
                if (id in f2h) return
                
                f2h [id] = this ['house.uuid']
            
                data.fias.push ({
                    id: id,
                    text: this ['fias.label'],
                })
            
            })
            
            data.record.f2h = f2h

            if (!data.record.fiashouseguid && data.fias.length == 1) data.record.fiashouseguid = data.fias [0].id

            done (data)

        })

    }

})