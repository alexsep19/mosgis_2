define ([], function () {

    var form_name = 'house_passport_form'

    $_DO.cancel_house_passport = function (e) {
        
        if (!confirm ('Отменить несохранённые правки?')) return

        var data = w2ui [form_name].record
        
        query ({type: 'houses'}, {}, function (data) {

            data.__read_only = true

            $_F5 (data)

        })

    }
    
    $_DO.edit_house_passport = function (e) {

        var data = {item: w2ui [form_name].record}

        data.__read_only = false
        
        var $form = w2ui [form_name]
                
        $_F5 (data)

    }

    $_DO.update_house_passport = function (e) {
    
        if (!confirm ('Сохранить изменения?')) return

        var d = w2ui [form_name].values ()
        
        var floorcount = parseInt (d.floorcount)
        var undergroundfloorcount = parseInt (d.undergroundfloorcount)
        var minfloorcount = parseInt (d.minfloorcount)

        if (floorcount > 0) {
        
            if (undergroundfloorcount > floorcount) die ('undergroundfloorcount', 'Подземных этажей не может быть больше, чем этажей всего')
            if (minfloorcount > floorcount) die ('minfloorcount', 'Минимальное число этажей не может превышать максимальное')
        
        }                

        if (d.kad_n) {
        
            if (!/^77:\d{2}:\d{7}:\d{4}$/.test (d.kad_n)) die ('kad_n', 'Данное значение не подходит для кадастрового номера по формату')
        
        }           

        if (!d.hasblocks) d.hasmultiplehouseswithsameadres = 0

        w2ui [form_name].lock ();

        query ({type: 'houses', action: 'update'}, {data: d}, function (data) {
        
            sessionStorage.setItem ('check_sum_area_fields_of_a_house', 1);
            location.reload ()
       
        })
        
    }

    $_DO.choose_tab_house_passport = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['passport_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('house_passport.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        w2ui ['topmost_layout'].unlock ('main')
        
        var data = $('body').data ('data')
        
        data.tabs = data.item.is_condo ? [
        
            {id: 'house_passport_entrances',    label: 'Подъезды'},
            {id: 'house_passport_lifts',        label: 'Лифты'},
            
        ]: [
        
            {id: 'house_passport_blocks',       label: 'Блоки', off: !data.item.hasblocks},
            
        ].filter (not_off)
        
        delete data.active_tab
            
        $.each (data.tabs, function () {
            
            if (this.id == localStorage.getItem ('house_passport.active_tab')) data.active_tab = this.id
            
        })

        if (!data.active_tab && data.tabs.length) data.active_tab = data.tabs [0].id

        data.__read_only = 1
        
        if (!data.item.is_condo) return done (data)
        
        query ({type: 'voc_nsi_list', id: 192, part: 'lines'}, {"cmd":"get","selected":[],"limit":100,"offset":0}, function (d) {

            var k = 'vc_nsi_192'

            var h = {}; h [k] = d.records.map (function (r) {return {id: r.code, label: r.f_ae990eb9f6}})

            add_vocabularies (h, h)

            data [k] = h [k]

            done (data)
        
        })        

    }

})