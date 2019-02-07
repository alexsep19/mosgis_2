define ([], function () {

    var form_name = 'account_item_popup_form'

    $_DO.load_premises_for_account_item_popup = function (e) {

        var form = w2ui [form_name]
        
        var v = form.values ()
        
        var premises = []
        
        function done () {
            form.get ('uuid_premise').options.items = premises
            form.refresh ()
        }
                
        if (!v.fiashouseguid) return done ()
        
        var uuid_house = form.record.f2h [v.fiashouseguid]

        if (!uuid_house) return done ()
        
        query ({type: 'premises', id: undefined}, {data: {uuid_house: uuid_house}}, function (d) {
        
            for (k in d) premises = d [k].map (function (i) {return {
                id: i.id,
                text: i.label,
            }})
            
            done ()

        })

    }

    $_DO.update_account_item_popup = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        
        if (!v.fiashouseguid) die ('fiashouseguid', 'Укажите, пожалуйста, адрес')
        if (!(v.sharepercent >= 0.01)) die ('sharepercent', 'Укажите, пожалуйста, корректное значение доли в процентах')
                
        var tia = {type: 'account_items', id: form.record.uuid}
        
        if (tia.id) {
            tia.action = 'update'
        }
        else {
            tia.action = 'create'
            v.uuid_account = $_REQUEST.id
        }

        query (tia, {data: v}, function (data) {
            w2popup.close ()
            var grid = w2ui ['account_common_items_grid']
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

        if (it.uuid_contract) {
        
            tia.type = 'contract_objects'
            
            p.search.push ({
                field:    "uuid_contract",
                operator: "is",
                value:    it.uuid_contract
            })
            
        }               
        
        query (tia, p, function (d) {
        
            var f2h = {}

            data.fias = d.root.map (function (i) {
            
                var id = i.fiashouseguid            
                
                f2h [id] = i ['house.uuid']
            
                return {
                    id: id,
                    text: i ['fias.label'],
                }
                
            })
            
            data.record.f2h = f2h

//            if (!data.record.fiashouseguid && data.fias.length == 1) data.record.fiashouseguid = data.fias [0].id

            done (data)

        })

    }

})