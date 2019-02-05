define ([], function () {

    $_DO.choose_tab_infrastructure = function (e) {

        var name = e.tab.id
                
        var layout = w2ui ['topmost_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }
            
        localStorage.setItem ('infrastructure.active_tab', name)
            
        use.block (name)
            
    }            

    return function (done) {        

        query ({type: 'infrastructures', part: 'vocs', id: undefined}, {}, function (data) {

            add_vocabularies (data, data)

            var ref_33_to_3 = []

            data.ref_33_to_3.items.forEach ((x, index, arr) => {

                if (ref_33_to_3.findIndex (y => {
                    return y.code_33 == x.code_33
                }) < 0) {

                    ref_33_to_3.push (data.ref_33_to_3.items.filter (z => z.code_33 == x.code_33).reduce ((accumulator, currentValue, currentIndex, array) => {
                        accumulator.code_3.push (data.vc_nsi_3.items.find (nsi_3 => nsi_3.id == currentValue.code_3))
                        return accumulator
                    }, {code_33: x.code_33, code_3: []}))

                }

            })

            data.ref_33_to_3 = ref_33_to_3

            query ({type: 'infrastructures'}, {}, function (d) {

                var it = data.item = d.item

                $('body').data ('data', data)

                var vc_nsi_2_filtered = {}

                data.vc_nsi_3.items.filter (x => data.item.codes_nsi_3.indexOf (x.id) >= 0)
                                   .forEach ((value, index, array) => {
                                        
                                        var code = value.nsi_2
                                        vc_nsi_2_filtered[code] = data.vc_nsi_2[value.nsi_2]

                                   })

                vc_nsi_2_filtered.items = data.vc_nsi_2.items.filter (x => Object.keys (vc_nsi_2_filtered).find (y => x.id == y))

                data.vc_nsi_2_filtered = vc_nsi_2_filtered

                function perms () {

                    if ((data.item.id_is_status != 10 && data.item.id_is_status != 11) || data.item.is_deleted)
                        return false;

                    if ($_USER.role.admin) return true

                    if ($_USER.role.nsi_20_8) {

                        var oktmos = Object.keys ($_USER.role).filter ((x) => x.startsWith ('oktmo_')).map ((x) => {
                            return x.substring ('oktmo_'.length)
                        })

                        if (data.item.manageroki == $_USER.uuid_org) {

                            if (!data.item.oktmo || oktmos.find (x => x == data.item.oktmo_code)) return true

                        }

                    }

                    if ($_USER.role.nsi_20_2) {

                        if (data.item.manageroki == $_USER.uuid_org) return true

                    }

                    return false

                }

                var mod_perms = perms ()

                data.item._can = {
                    edit: mod_perms,
                    approve: mod_perms,
                    update: mod_perms,
                    cancel: mod_perms,
                    delete: mod_perms,
                }

                if (!data.item.is_deleted) {

                    switch (data.item.id_is_status) {
                        case 14:
                        case 34:
                            it._can.alter = 1
                    } 

                }            
                               
                data.item.okitype = data.vc_nsi_33[data.item.code_vc_nsi_33]
                data.item.is_object = data.vc_nsi_33.items.find (x => x.id == data.item.code_vc_nsi_33).type == "Объект"

                console.log (data.item)

                done (data)

            })

        })

    }

})